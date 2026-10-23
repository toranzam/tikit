package com.tikit.seller.controller;

import com.tikit.common.dto.ResponseDTO;
import com.tikit.seller.dto.SellerApplicationRequestDTO;
import com.tikit.seller.dto.SellerApplicationResponseDTO;
import com.tikit.seller.service.SellerApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SellerApplicationController {

    private final SellerApplicationService service;

    //판매자 등록 신청
    @PostMapping("/seller-applications")
    public ResponseEntity<ResponseDTO<SellerApplicationResponseDTO>> apply(
            @AuthenticationPrincipal String accountId, @RequestBody SellerApplicationRequestDTO dto){

        SellerApplicationResponseDTO result = service.apply(Long.valueOf(accountId), dto);

        Object HttpStaus;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.success("판매자 등록신청이 접수되었습니다", result));
    }

    //판매자 신청 결과 조회
    @GetMapping("/me/seller-application")
    public ResponseEntity<ResponseDTO<SellerApplicationResponseDTO>> getMyApplication(
            @AuthenticationPrincipal String accountId){

        SellerApplicationResponseDTO result = service.getMyApplication(Long.valueOf(accountId));

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseDTO.success("판매자 신청결과 조회 성공", result));

    }

}

