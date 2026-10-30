package com.tikit.payment.service;

import com.tikit.auth.entity.Member;
import com.tikit.auth.entity.MemberStatus;
import com.tikit.auth.entity.Role;
import com.tikit.booking.entity.Booking;
import com.tikit.booking.entity.BookingStatus;
import com.tikit.common.ErrorCode;
import com.tikit.payment.dto.request.PaymentConfirmRequestDTO;
import com.tikit.payment.entity.PaymentAttempt;
import com.tikit.payment.exception.PaymentException;
import com.tikit.payment.repository.PaymentAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentConfirmTxService {

    private final PaymentAttemptRepository paymentAttemptRepository;

    // 승인 1차 트랜잭션: 회원 -> 예매 -> 결제 순으로 잠그고 검사한 뒤 PROCESSING 저장 (커밋 후 Toss 호출)
    @Transactional
    public ConfirmationStart startConfirmation(Long paymentAttemptId, Long memberId,
                                               PaymentConfirmRequestDTO request) {
        String detail = "paymentAttemptId=" + paymentAttemptId + ", memberId=" + memberId;
        Instant now = Instant.now();

        // 1. 결제 시도의 예매 ID 조회 (없으면 404)
        Long bookingId = paymentAttemptRepository.findBookingIdById(paymentAttemptId)
                .orElseThrow(() -> new PaymentException(ErrorCode.RESOURCE_NOT_FOUND, detail));

        // 2. 회원 잠금
        Member member = paymentAttemptRepository.findMemberForUpdate(memberId)
                .orElseThrow(() -> new PaymentException(ErrorCode.RESOURCE_NOT_FOUND, detail));

        // 3. 본인 예매 잠금 (다른 회원 결제면 결과 없음 -> 없는 결제와 같은 404)
        Booking booking = paymentAttemptRepository.findOwnedBookingForUpdate(bookingId, memberId)
                .orElseThrow(() -> new PaymentException(ErrorCode.RESOURCE_NOT_FOUND, detail));

        // 토큰이 아닌 현재 DB 기준으로 회원 상태와 역할 확인
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new PaymentException(ErrorCode.ACCOUNT_RESTRICTED, detail);
        }
        if (member.getRole() != Role.MEMBER) {
            throw new PaymentException(ErrorCode.ACCESS_DENIED, detail);
        }

        // 4. 결제 시도 잠금 (잠금 후 읽어서 최신 상태 보장)
        PaymentAttempt attempt = paymentAttemptRepository.findByIdForUpdate(paymentAttemptId)
                .orElseThrow(() -> new PaymentException(ErrorCode.RESOURCE_NOT_FOUND, detail));

        // 요청 주문 번호, 금액을 DB 값과 대조 (요청 값은 믿지 않음)
        if (!attempt.getOrderId().equals(request.getOrderId())
                || !attempt.getAmount().equals(request.getAmount())) {
            throw new PaymentException(ErrorCode.PAYMENT_AMOUNT_MISMATCH, detail);
        }

        // 이미 승인된 결제는 Toss를 다시 부르지 않고 현재 결과 반환 (200)
        if (attempt.isApproved()) {
            return toResult(ConfirmationStartStatus.ALREADY_APPROVED, attempt);
        }

        // 처리 중 또는 결과 확인 중이면 Toss를 다시 부르지 않음 (202, 프론트가 조회 API로 재조회)
        if (attempt.isInProgress()) {
            return toResult(ConfirmationStartStatus.IN_PROGRESS, attempt);
        }

        // READY가 아니면(FAILED 등) 이 결제 시도로는 승인 불가 (409)
        if (!attempt.isReady()) {
            throw new PaymentException(ErrorCode.INVALID_PAYMENT_STATE, detail);
        }

        // 예매가 결제 대기 중이 아니면 승인 불가 (만료면 BOOKING_EXPIRED, 확정, 취소면 INVALID_PAYMENT_STATE)
        if (!booking.isPendingPayment()) {
            ErrorCode errorCode = booking.getStatus() == BookingStatus.EXPIRED
                    ? ErrorCode.BOOKING_EXPIRED
                    : ErrorCode.INVALID_PAYMENT_STATE;
            throw new PaymentException(errorCode, detail);
        }

        // 결제 기한(제한시간) 확인 (예매 파트와 같은 기준: now >= expiresAt이면 만료)
        if (booking.isExpired(now)) {
            throw new PaymentException(ErrorCode.BOOKING_EXPIRED, detail);
        }

        // PROCESSING에 저장 (같은 결제의 다음 승인 요청은 이 상태를 보고 Toss를 다시 부르지 않음)
        attempt.startConfirmation(request.getPaymentKey(), now);

        // TODO 복구 작업 저장

        log.info("payment confirmation started: paymentAttemptId={}, bookingId={}", paymentAttemptId, bookingId);

        return toResult(ConfirmationStartStatus.STARTED, attempt);
    }

    // 엔티티에서 Toss 호출에 필요한 값만 꺼내 결과로 반환
    private ConfirmationStart toResult(ConfirmationStartStatus status, PaymentAttempt attempt) {
        return new ConfirmationStart(
                status,
                attempt.getId(),
                attempt.getPaymentKey(),
                attempt.getOrderId(),
                attempt.getAmount(),
                attempt.getPgIdempotencyKey()
        );
    }
}
