
package com.tikit.seat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "session_grade_prices")
@IdClass(SessionGradePriceId.class)
@Getter
@NoArgsConstructor
public class SessionGradePrice {

    @Id
    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Id
    @Column(name = "grade", nullable = false, length = 20)
    private String grade;

    @Column(name = "price", nullable = false)
    private Long price;
}
