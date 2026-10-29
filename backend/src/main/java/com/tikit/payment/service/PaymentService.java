package com.tikit.payment.service;

import com.tikit.payment.dto.response.PaymentAttemptResponseDTO;
import com.tikit.payment.entity.PaymentAttemptStatus;
import com.tikit.payment.exception.PaymentNotFoundException;
import com.tikit.payment.repository.PaymentAttemptDetailView;
import com.tikit.payment.repository.PaymentAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final String BOOKING_PENDING_PAYMENT = "PENDING_PAYMENT";
    private static final int ORDER_NAME_MAX_LENGTH = 100;

    private final PaymentAttemptRepository paymentAttemptRepository;

    // 결제 처리 결과 조회 (Toss를 호출하지 않고 DB 상태만 반환)
    @Transactional(readOnly = true)
    public PaymentAttemptResponseDTO getPaymentAttempt(Long paymentAttemptId, Long memberId) {
        PaymentAttemptDetailView view = paymentAttemptRepository
                .findDetailByIdAndMemberId(paymentAttemptId, memberId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "paymentAttemptId=" + paymentAttemptId + ", memberId=" + memberId));

        OffsetDateTime now = OffsetDateTime.now(SEOUL);
        OffsetDateTime expiresAt = view.getExpiresAt().atZoneSameInstant(SEOUL).toOffsetDateTime();
        PaymentAttemptStatus status = PaymentAttemptStatus.valueOf(view.getStatus());

        return PaymentAttemptResponseDTO.builder()
                .paymentAttemptId(view.getPaymentAttemptId())
                .bookingId(view.getBookingId())
                .orderId(view.getOrderId())
                .orderName(buildOrderName(view.getPerformanceTitle(), view.getSeatCount()))
                .customerKey(view.getCustomerKey())
                .amount(view.getAmount())
                .currency(view.getCurrency())
                .status(status)
                .bookingStatus(view.getBookingStatus())
                .expiresAt(expiresAt)
                .retryAllowed(isRetryAllowed(status, view.getBookingStatus(), expiresAt, now))
                .failureCode(view.getFailureCode())
                .serverTime(now)
                .build();
    }

    // 최종 실패이고, 예매가 결제 대기 중이며, 결제 기한 안일 때만 새 결제 시도 가능
    private boolean isRetryAllowed(PaymentAttemptStatus status, String bookingStatus,
                                   OffsetDateTime expiresAt, OffsetDateTime now) {
        return status == PaymentAttemptStatus.FAILED
                && BOOKING_PENDING_PAYMENT.equals(bookingStatus)
                && now.isBefore(expiresAt);
    }

    // 결제창 주문명: 1매면 "공연명", 2매 이상이면 "공연명 외 N매" (최대 100자)
    private String buildOrderName(String performanceTitle, Integer seatCount) {
        String suffix = (seatCount != null && seatCount > 1) ? " 외 " + (seatCount - 1) + "매" : "";
        int maxTitleLength = ORDER_NAME_MAX_LENGTH - suffix.length();

        String title = performanceTitle.length() > maxTitleLength
                ? performanceTitle.substring(0, maxTitleLength)
                : performanceTitle;

        return title + suffix;
    }
}