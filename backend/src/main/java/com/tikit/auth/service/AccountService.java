package com.tikit.auth.service;

import com.tikit.auth.dto.JwtTokenDTO;
import com.tikit.auth.dto.request.AccountLoginRequestDTO;
import com.tikit.auth.dto.request.AccountRegisterRequestDTO;
import com.tikit.auth.entity.Account;
import com.tikit.auth.entity.Role;
import com.tikit.auth.exception.LoginException;
import com.tikit.auth.exception.RefreshTokenException;
import com.tikit.auth.redis.RefreshTokenRepository;
import com.tikit.auth.repository.AccountRepository;
import com.tikit.util.JWTProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JWTProvider jwtProvider;

    @Transactional
    public String registerUser(AccountRegisterRequestDTO dto) {

        String encodedPw = passwordEncoder.encode(dto.getRawPassword());

        Account account = Account.create(
                dto.getEmail(),
                encodedPw,
                dto.getRealName(),
                Role.USER
        );

        Account saved = accountRepository.save(account);

        log.info("registered user: {}", saved.getAccountId());

        return saved.getRealName();
    }

    @Transactional(readOnly = true)
    public JwtTokenDTO login(AccountLoginRequestDTO dto) {

        Account account = accountRepository.findByEmail(dto.getEmail())
                .orElseThrow(() ->
                        new LoginException("Account Not Found"));

        if (!passwordEncoder.matches(dto.getRawPassword(), account.getPassword())) {
            throw new LoginException("Invalid Password");
        }

        String accessToken = jwtProvider.generateToken(account, 10);
        String refreshToken = jwtProvider.generateToken(account, 60);

        refreshTokenRepository.save(account.getAccountId().toString(), refreshToken);

        log.info("login success: {}", account.getAccountId());

        return new JwtTokenDTO(accessToken, refreshToken);
    }

    @Transactional(readOnly = true)
    public JwtTokenDTO refresh(String refreshToken) {


        Claims claims;
        // refreshToken 에서 정보 추출 및 토큰 만료 확인
        try {
            claims = jwtProvider.getClaims(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new RefreshTokenException("Refresh Token이 만료되었습니다.");
        } catch (JwtException | IllegalArgumentException e) {
            throw new RefreshTokenException("유효하지 않은 Refresh Token입니다.");
        }

        String accountId = claims.getSubject();
        String email = claims.get("email", String.class);
        String roleName = claims.get("roleName", String.class);

        // redis 에서 확인
        Optional<String> redisRefreshToken = refreshTokenRepository.find(accountId);

        if (redisRefreshToken.isEmpty()) {
            throw new RefreshTokenException("저장된 세션이 존재하지 않습니다.");
        }

        String savedRefreshToken = redisRefreshToken.get();

        if (!savedRefreshToken.equals(refreshToken)) {
            // 파기된 rt가 들어온경우
            refreshTokenRepository.delete(accountId);

            throw new RefreshTokenException("토큰 재사용 감지. 세션를 종료합니다.");
        }

        Account account = accountRepository.findById(Long.valueOf(accountId))
                .orElseThrow(() -> new RefreshTokenException("계정이 존재하지 않습니다."));

        String newAccessToken = jwtProvider.generateToken(account, 10);
        String newRefreshToken = jwtProvider.generateToken(account, 60);

        refreshTokenRepository.save(accountId, newRefreshToken);

        return new JwtTokenDTO(newAccessToken, newRefreshToken);
    }

}
