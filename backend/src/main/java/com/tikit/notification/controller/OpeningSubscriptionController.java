package com.tikit.notification.controller;

import com.tikit.common.dto.ResponseDTO;
import com.tikit.notification.entity.OpeningSubscription;
import com.tikit.notification.service.OpeningSubscriptionService;
import com.tikit.notification.dto.NotificationSettingUpdateRequestDTO;
import com.tikit.common.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


// 공연별 예매 시작 알림 신청 API
@RestController
@RequestMapping("/api/v1/opening-subscriptions")
@RequiredArgsConstructor
public class OpeningSubscriptionController {

    private final OpeningSubscriptionService subscriptionService;

    private Long getMemberId(String accountId) {
        if (accountId == null) {
            throw new org.springframework.security.authentication
                    .AuthenticationCredentialsNotFoundException(
                    "로그인이 필요합니다."
            );
        }

        return Long.valueOf(accountId);
    }

    // 공연별 알림 신청
    @PostMapping("/{performanceId}")
    public ResponseEntity<ResponseDTO<Void>> subscribe(
            @AuthenticationPrincipal String accountId,
            @PathVariable Long performanceId
    ) {
        Long memberId = getMemberId(accountId);

        subscriptionService.subscribe(memberId, performanceId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseDTO.success("알림 신청 성공"));
    }

    // 공연별 알림 신청 <<해제>>
    @DeleteMapping("/{performanceId}")
    public ResponseEntity<ResponseDTO<Void>> unsubscribe(
            @AuthenticationPrincipal String accountId,
            @PathVariable Long performanceId
    ) {
        Long memberId = getMemberId(accountId);

        subscriptionService.unsubscribe(memberId, performanceId);

        return ResponseEntity
                .ok(ResponseDTO.success("알림 신청 해제 성공"));
    }

    // 회원별 알림 신청 공연 ID 목록 조회
    @GetMapping
    public ResponseEntity<ResponseDTO<List<Long>>> getSubscriptions(
            @AuthenticationPrincipal String accountId
    ) {
        Long memberId = getMemberId(accountId);

        List<Long> performanceIds = subscriptionService
                .getSubscriptions(memberId)
                .stream()
                .map(OpeningSubscription::getPerformanceId)
                .toList();

        return ResponseEntity.ok(
                ResponseDTO.success("알림 신청 목록 조회 성공", performanceIds)
        );
    }

    // 특정 공연 알림 신청 여부 조회
    @GetMapping("/{performanceId}")
    public ResponseEntity<ResponseDTO<Boolean>> isSubscribed(
            @AuthenticationPrincipal String accountId,
            @PathVariable Long performanceId
    ) {
        Long memberId = getMemberId(accountId);

        boolean subscribed =
                subscriptionService.isSubscribed(memberId, performanceId);

        return ResponseEntity.ok(
                ResponseDTO.success("알림 신청 상태 조회 성공", subscribed)
        );
    }

    // 전체 알림 수신 설정 조회
    @GetMapping("/settings")
    public ResponseEntity<ResponseDTO<Boolean>> getNotificationSetting(
            @AuthenticationPrincipal String accountId
    ) {
        Long memberId = getMemberId(accountId);

        boolean enabled = subscriptionService.getNotificationEnabled(memberId);

        return ResponseEntity.ok(
                ResponseDTO.success("전체 알림 수신 설정 조회 성공", enabled)
        );
    }

    // 전체 알림 수신 설정 <<변경>>
    @PatchMapping("/settings")
    public ResponseEntity<ResponseDTO<?>> updateNotificationSetting(
            @AuthenticationPrincipal String accountId,
            @RequestBody NotificationSettingUpdateRequestDTO request
    ) {
        Long memberId = getMemberId(accountId);

        // 요청값 누락 확인
        if (request == null || request.enabled() == null) {
            ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;

            return ResponseEntity
                    .status(errorCode.getStatus())
                    .body(ResponseDTO.fail(
                            errorCode,
                            "전체 알림 수신 설정값을 입력해 주세요."
                    ));
        }

        boolean enabled = subscriptionService.updateNotificationEnabled(
                memberId,
                request.enabled()
        );

        return ResponseEntity.ok(
                ResponseDTO.success("전체 알림 수신 설정 변경 성공", enabled)
        );
    }
}
