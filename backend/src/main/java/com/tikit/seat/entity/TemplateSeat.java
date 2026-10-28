
package com.tikit.seat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "template_seats")
@Getter
@NoArgsConstructor
public class TemplateSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(name = "seat_code", nullable = false)
    private String seatCode;

    @Column(nullable = false)
    private String label;

    @Column(name = "row_label", nullable = false)
    private String rowLabel;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @Column(nullable = false)
    private String grade;

    @Column(nullable = false)
    private Integer x;

    @Column(nullable = false)
    private Integer y;
}
