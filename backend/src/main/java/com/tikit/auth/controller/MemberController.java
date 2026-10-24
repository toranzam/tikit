package com.tikit.auth.controller;

import com.tikit.auth.dto.response.JwtTokenResponseDTO;
import com.tikit.auth.dto.request.MemberLoginRequestDTO;
import com.tikit.auth.dto.request.MemberRegisterRequestDTO;
import com.tikit.auth.service.MemberService;
import com.tikit.common.dto.ResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;


    @PostMapping("/login")
    public ResponseEntity<ResponseDTO<JwtTokenResponseDTO>> login(@RequestBody MemberLoginRequestDTO dto) {
        JwtTokenResponseDTO tokenDTO = memberService.login(dto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseDTO.success("로그인 성공", tokenDTO));
    }

    @PostMapping("/signup")
    public ResponseEntity<ResponseDTO<Void>> signup(@RequestBody MemberRegisterRequestDTO dto) {
        memberService.signupMember(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseDTO.success("회원가입 성공."));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ResponseDTO<JwtTokenResponseDTO>> refresh(@RequestBody JwtTokenResponseDTO jwtTokenResponseDTO) {

        JwtTokenResponseDTO newJwtToken = memberService.refresh(jwtTokenResponseDTO.getRefreshToken());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseDTO.success(
                                "토큰 재발급 성공",
                                newJwtToken
                        )
                );
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseDTO<Void>> logout(@AuthenticationPrincipal String accountId) {
        memberService.logout(accountId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseDTO.success("로그아웃 성공"));
    }

}
