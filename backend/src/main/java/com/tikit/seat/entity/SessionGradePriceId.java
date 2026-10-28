
package com.tikit.seat.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SessionGradePriceId implements Serializable {

    private Long sessionId;
    private String grade;
}
