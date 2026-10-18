package com.tikit.auth.controller;

import com.tikit.auth.dto.request.AccountLoginRequestDTO;
import com.tikit.auth.dto.request.AccountRegisterRequestDTO;
import com.tikit.auth.dto.response.AccountLoginResponseDTO;
import com.tikit.auth.entity.Account;
import com.tikit.auth.service.AccountService;
import com.tikit.common.dto.ResponseDTO;
import com.tikit.util.JWTProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final JWTProvider jwtProvider;

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO<AccountLoginResponseDTO>> login(@RequestBody AccountLoginRequestDTO dto) {
        Account account = accountService.login(dto);

        String accessToken = jwtProvider.generateToken(account, 10);
        String refreshToken = jwtProvider.generateToken(account, 60);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDTO<>(1, "로그인 성공", new AccountLoginResponseDTO(accessToken, refreshToken)));
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseDTO<Void>> register(@RequestBody AccountRegisterRequestDTO dto) {
        String s = accountService.registerUser(dto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDTO<>(1, "회원가입이 성공하였습니다.", null));
    }


}
