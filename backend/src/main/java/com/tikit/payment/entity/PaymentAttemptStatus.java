package com.tikit.payment.entity;

public enum PaymentAttemptStatus {
    READY,          // 결제 준비됨 (주문 ID와 금액 발급)
    PROCESSING,     // Toss 승인 요청 중
    APPROVED,       // 승인 완료
    RECONCILING,    //결과 불명확, 조회로 확인 중
    FAILED          // 승인 실패
}
