package com.tikit.payment.repository;

import java.time.OffsetDateTime;

public interface PaymentAttemptDetailView {

    Long getPaymentAttemptId();

    Long getBookingId();

    String getOrderId();

    String getCustomerKey();

    Long getAmount();

    String getCurrency();

    String getStatus();

    String getFailureCode();

    String getBookingStatus();

    OffsetDateTime getExpiresAt();

    String getPerformanceTitle();

    Integer getSeatCount();
}
