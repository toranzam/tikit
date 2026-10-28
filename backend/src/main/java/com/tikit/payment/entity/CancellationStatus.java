package com.tikit.payment.entity;

public enum CancellationStatus {
    REQUESTED,      // 취소 요청 접수, 환불 금액 확정
    PROCESSING,     // Toss 취소 요청 중
    SUCCEEDED,      // 취소·환불 완료
    RECONCILING,    // 결과 불명확, 조회로 확인 중
    FAILED          // 취소 실패
}
