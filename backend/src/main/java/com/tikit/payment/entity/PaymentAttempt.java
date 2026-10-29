package com.tikit.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EntityListeners(AuditingEntityListener.class)
@Table(name = "payment_attempts")
public class PaymentAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "order_id", nullable = false, unique = true, length = 64)
    private String orderId;

    @Column(name = "payment_key", unique = true, length = 200)
    private String paymentKey;

    @Column(name = "pg_idempotency_key", nullable = false, unique = true, length = 128)
    private String pgIdempotencyKey;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "KRW";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentAttemptStatus status = PaymentAttemptStatus.READY;

    @Column(name = "failure_code", length = 60)
    private String failureCode;

    @Column(name = "confirmation_requested_at")
    private Instant confirmationRequestedAt;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static PaymentAttempt create(Long bookingId, String orderId, Long amount, String pgIdempotencyKey) {
        PaymentAttempt attempt = new PaymentAttempt();
        attempt.bookingId = bookingId;
        attempt.orderId = orderId;
        attempt.amount = amount;
        attempt.pgIdempotencyKey = pgIdempotencyKey;

        return attempt;
    }
}
