package com.tikit.auth.service;

import com.tikit.auth.dto.response.JwtTokenResponseDTO;
import com.tikit.auth.dto.request.MemberLoginRequestDTO;
import com.tikit.auth.dto.request.MemberRegisterRequestDTO;
import com.tikit.auth.entity.Member;
import com.tikit.auth.entity.Role;
import com.tikit.auth.exception.LoginException;
import com.tikit.auth.exception.RefreshTokenException;
import com.tikit.auth.redis.RefreshTokenRepository;
import com.tikit.auth.repository.AccountRepository;
import com.tikit.config.security.CustomUserDetails;
import com.tikit.util.JWTProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JWTProvider jwtProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public void signupUser(MemberRegisterRequestDTO dto) {

        String encodedPw = passwordEncoder.encode(dto.getPassword());

        Member member = Member.create(
                dto.getEmail(),
                encodedPw,
                dto.getRealName(),
                Role.MEMBER
        );

        Member saved = accountRepository.save(member);

        log.info("registered user: {}", saved.getId());

    }

    @Transactional(readOnly = true)
    public JwtTokenResponseDTO login(MemberLoginRequestDTO dto) {

        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            dto.getEmail(),
                            dto.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new LoginException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Member member = userDetails.getMember();

        String accessToken = jwtProvider.generateAccessToken(member);
        String refreshToken = jwtProvider.generateRefreshToken(member);

        refreshTokenRepository.save(member.getId().toString(), refreshToken);

        log.info("login success: {}", member.getId());

        return new JwtTokenResponseDTO(accessToken, refreshToken);
    }

    @Transactional(readOnly = true)
    public JwtTokenResponseDTO refresh(String refreshToken) {

        Claims claims;
        // refreshToken 에서 정보 추출 및 토큰 만료 확인
        try {
            claims = jwtProvider.getClaims(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new RefreshTokenException("Refresh Token이 만료되었습니다.");
        } catch (JwtException | IllegalArgumentException e) {
            throw new RefreshTokenException("유효하지 않은 Refresh Token입니다.");
        }

        if (!"REFRESH".equals(claims.get("tokenType", String.class))) {
            throw new RefreshTokenException("Refresh Token이 아닙니다.");
        }

        String accountId = claims.getSubject();

        Member member = accountRepository.findById(Long.valueOf(accountId))
                .orElseThrow(() -> new RefreshTokenException("계정이 존재하지 않습니다."));

        String newAccessToken = jwtProvider.generateAccessToken(member);
        String newRefreshToken = jwtProvider.generateRefreshToken(member);

        long result = refreshTokenRepository.rotate(
                accountId,
                refreshToken,
                newRefreshToken
        );

        if (result == 0) {
            throw new RefreshTokenException("저장된 세션이 존재하지 않습니다.");
        }

        if (result == -1) {
            throw new RefreshTokenException("토큰 재사용 감지. 세션을 종료합니다.");
        }


        return new JwtTokenResponseDTO(newAccessToken, newRefreshToken);
    }

    public void logout(String accountId) {
        refreshTokenRepository.delete(accountId);

    }
}
