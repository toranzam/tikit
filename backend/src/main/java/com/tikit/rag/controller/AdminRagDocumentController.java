package com.tikit.rag.controller;

import com.tikit.common.dto.ResponseDTO;
import com.tikit.rag.dto.request.RagDocumentCreateRequest;
import com.tikit.rag.dto.response.RagDocumentDetailResponse;
import com.tikit.rag.dto.response.RagDocumentPageResponse;
import com.tikit.rag.service.RagDocumentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/admin/rag/documents")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRagDocumentController {

    private final RagDocumentService ragDocumentService;

    // 공통 정책 문서 등록
    @PostMapping
    public ResponseEntity<ResponseDTO<RagDocumentDetailResponse>> createDocument(
            @AuthenticationPrincipal String accountId,
            @Valid @RequestBody RagDocumentCreateRequest request
    ) {
        Long adminId = Long.valueOf(accountId);

        RagDocumentDetailResponse response =
                ragDocumentService.createPolicy(adminId, request);

        URI location = URI.create(
                "/api/v1/admin/rag/documents/" + response.id()
        );

        return ResponseEntity.created(location)
                .body(ResponseDTO.success("문서가 등록되었습니다.", response));
    }

    // 공통 정책 문서 목록 조회
    @GetMapping
    public ResponseEntity<ResponseDTO<RagDocumentPageResponse>> getDocuments(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "페이지는 0 이상이어야 합니다.") int page,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
            @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다.") int size
    ) {
        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "id")
        );

        RagDocumentPageResponse response = RagDocumentPageResponse.from(
                ragDocumentService.getDocuments(pageable)
        );

        return ResponseEntity.ok(
                ResponseDTO.success("문서 목록을 조회했습니다.", response)
        );
    }

    // 공통 정책 문서 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<RagDocumentDetailResponse>> getDocument(
            @PathVariable("id")
            @Positive(message = "문서 ID는 양수여야 합니다.") Long documentId
    ) {
        RagDocumentDetailResponse response =
                ragDocumentService.getDocument(documentId);

        return ResponseEntity.ok(
                ResponseDTO.success("문서 상세 내용을 조회했습니다.", response)
        );
    }
}
