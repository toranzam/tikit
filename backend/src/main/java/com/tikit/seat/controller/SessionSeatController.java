
package com.tikit.seat.controller;

import com.tikit.seat.dto.response.SessionSeatResponseDTO;
import com.tikit.seat.service.SessionSeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class SessionSeatController {

    private final SessionSeatService sessionSeatService;

    @GetMapping("/{sessionId}/seats")
    public ResponseEntity<List<SessionSeatResponseDTO>> getSeats(
            @PathVariable Long sessionId) {

        List<SessionSeatResponseDTO> seats =
                sessionSeatService.getSeats(sessionId);

        return ResponseEntity.ok(seats);
    }
}
