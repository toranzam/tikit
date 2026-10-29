
package com.tikit.seat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "session_seats")
@Getter
@NoArgsConstructor
public class SessionSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "seat_template_id", nullable = false)
    private Long seatTemplateId;

    @Column(name = "template_seat_id", nullable = false)
    private Long templateSeatId;

    @Column(nullable = false)
    private String grade;

    @Column(nullable = false)
    private String status;

    @Column(name = "current_booking_id")
    private Long currentBookingId;

    @Version
    private Long version;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
