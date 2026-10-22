package com.tikit.seat.dto.response;

public record SessionSeatResponseDTO(
        Long seatId,
        String seatCode,
        String label,
        String rowLabel,
        Integer seatNumber,
        String grade,
        Integer x,
        Integer y,
        String status
) {
}