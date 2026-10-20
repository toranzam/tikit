package com.tikit.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "접근 권한이 없거나 현재 상태에서는 접근할 수 없습니다."
    ),
    ACCOUNT_RESTRICTED(
            HttpStatus.FORBIDDEN,
            "이용이 제한된 계정입니다."
    ),
    ADMIN_ACCOUNT_PROTECTED(
            HttpStatus.FORBIDDEN,
            "해당 API로 관리자 계정을 변경할 수 없습니다."
    ),
    ADMISSION_INVALID(
            HttpStatus.FORBIDDEN,
            "대기열 입장 권한이 없거나 만료되었습니다."
    ),
    AUTHENTICATION_REQUIRED(
            HttpStatus.UNAUTHORIZED,
            "인증이 필요합니다."
    ),
    BOOKING_ALREADY_CONFIRMED(
            HttpStatus.CONFLICT,
            "이미 확정된 예매입니다. 전체 취소 절차를 이용해 주세요."
    ),
    BOOKING_EXPIRED(
            HttpStatus.CONFLICT,
            "예매 결제 제한시간이 만료되었습니다."
    ),
    BOOKING_LIMIT_EXCEEDED(
            HttpStatus.CONFLICT,
            "회차별 예매 가능 매수를 초과했습니다."
    ),
    CANCELLATION_IN_PROGRESS(
            HttpStatus.CONFLICT,
            "이미 전체 취소 요청을 처리하고 있습니다."
    ),
    CANCELLATION_NOT_ALLOWED(
            HttpStatus.CONFLICT,
            "현재 예매 상태 또는 취소 기한에 따라 취소할 수 없습니다."
    ),
    CANCELLATION_QUOTE_CHANGED(
            HttpStatus.CONFLICT,
            "예상 환불액 또는 취소 정책이 변경되었습니다. 다시 확인해 주세요."
    ),
    EMAIL_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "이미 가입된 이메일입니다."
    ),
    IDEMPOTENCY_CONFLICT(
            HttpStatus.CONFLICT,
            "동일한 멱등키로 다른 요청 내용을 전달할 수 없습니다."
    ),
    INDEX_NOT_READY(
            HttpStatus.CONFLICT,
            "해당 문서 버전의 색인이 아직 완료되지 않았습니다."
    ),
    INVALID_BOOKING_STATE(
            HttpStatus.CONFLICT,
            "현재 예매 상태에서는 결제를 시작할 수 없습니다."
    ),
    INVALID_CREDENTIALS(
            HttpStatus.UNAUTHORIZED,
            "로그인 정보를 확인해 주세요."
    ),
    INVALID_PAYMENT_STATE(
            HttpStatus.CONFLICT,
            "현재 결제 상태에서는 새로운 승인 요청을 할 수 없습니다."
    ),
    INVALID_SALES_CONFIGURATION(
            HttpStatus.BAD_REQUEST,
            "회차, 가격 또는 판매 일정 설정을 확인해 주세요."
    ),
    KOPIS_ALREADY_REGISTERED(
            HttpStatus.CONFLICT,
            "이미 등록된 KOPIS 공연입니다."
    ),
    KOPIS_UNAVAILABLE(
            HttpStatus.SERVICE_UNAVAILABLE,
            "KOPIS 조회 서비스를 일시적으로 사용할 수 없습니다."
    ),
    PASSWORD_MISMATCH(
            HttpStatus.BAD_REQUEST,
            "비밀번호가 일치하지 않습니다."
    ),
    PAYMENT_AMOUNT_MISMATCH(
            HttpStatus.BAD_REQUEST,
            "요청한 주문 또는 결제 금액이 저장된 정보와 일치하지 않습니다."
    ),
    PAYMENT_DECLINED(
            HttpStatus.BAD_REQUEST,
            "결제가 승인되지 않았거나 거절되었습니다."
    ),
    PAYMENT_IN_PROGRESS(
            HttpStatus.CONFLICT,
            "기존 결제 승인 결과를 확인 중입니다. 잠시 후 다시 확인해 주세요."
    ),
    PERFORMANCE_EDIT_LOCKED(
            HttpStatus.CONFLICT,
            "현재 심사, 게시 또는 예매 상태에서는 공연을 수정할 수 없습니다."
    ),
    PUBLICATION_NOT_APPROVED(
            HttpStatus.CONFLICT,
            "게시 승인되지 않은 공연입니다."
    ),
    PUBLICATION_REQUIREMENTS_MISSING(
            HttpStatus.BAD_REQUEST,
            "게시 심사에 필요한 판매 설정 또는 권한 자료가 누락되었습니다."
    ),
    RAG_UNAVAILABLE(
            HttpStatus.SERVICE_UNAVAILABLE,
            "AI 안내 서비스를 일시적으로 사용할 수 없습니다."
    ),
    RATE_LIMITED(
            HttpStatus.TOO_MANY_REQUESTS,
            "요청 횟수 제한을 초과했습니다. 잠시 후 다시 시도해 주세요."
    ),
    REFRESH_TOKEN_INVALID(
            HttpStatus.UNAUTHORIZED,
            "유효하지 않은 리프레시 토큰입니다. 다시 로그인해 주세요."
    ),
    RESOURCE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "요청한 리소스를 찾을 수 없거나 접근할 수 없습니다."
    ),
    REVIEW_ALREADY_DECIDED(
            HttpStatus.CONFLICT,
            "이미 승인 또는 반려된 심사입니다."
    ),
    SALE_NOT_OPEN(
            HttpStatus.CONFLICT,
            "판매 시작 전이거나 판매가 종료되었습니다."
    ),
    SALE_RESUME_NOT_ALLOWED(
            HttpStatus.CONFLICT,
            "현재 승인 또는 공개 상태에서는 판매를 재개할 수 없습니다."
    ),
    SALE_STOPPED(
            HttpStatus.CONFLICT,
            "신규 판매가 중단되었습니다."
    ),
    SEAT_UNAVAILABLE(
            HttpStatus.CONFLICT,
            "선택한 좌석 중 예매할 수 없는 좌석이 있습니다."
    ),
    SELLER_APPLICATION_EXISTS(
            HttpStatus.CONFLICT,
            "진행 중인 판매자 신청이 있거나 이미 승인된 판매자입니다."
    ),
    SERVICE_UNAVAILABLE(
            HttpStatus.SERVICE_UNAVAILABLE,
            "의존 서비스를 일시적으로 사용할 수 없습니다."
    ),
    STATE_CONFLICT(
            HttpStatus.CONFLICT,
            "현재 상태 또는 버전과 요청이 충돌합니다."
    ),
    VALIDATION_ERROR(
            HttpStatus.BAD_REQUEST,
            "요청 형식 또는 입력값을 확인해 주세요."
    ),
    VERSION_CONFLICT(
            HttpStatus.CONFLICT,
            "조회한 이후 설정이 변경되었습니다. 다시 조회해 주세요."
    ),
    WITHDRAWAL_BLOCKED(
            HttpStatus.CONFLICT,
            "진행 중인 거래 또는 판매자 운영 의무로 탈퇴할 수 없습니다."
    );


    private final HttpStatus status;
    private final String message;
}
