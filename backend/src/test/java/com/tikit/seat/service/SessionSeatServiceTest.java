
package com.tikit.seat.service;

import com.tikit.seat.dto.response.SessionSeatResponseDTO;
import com.tikit.seat.entity.SessionSeat;
import com.tikit.seat.entity.TemplateSeat;
import com.tikit.seat.repository.PerformanceSessionRepository;
import com.tikit.seat.repository.SessionSeatRepository;
import com.tikit.seat.repository.TemplateSeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionSeatServiceTest {

    @Mock
    private PerformanceSessionRepository performanceSessionRepository;

    @Mock
    private SessionSeatRepository sessionSeatRepository;

    @Mock
    private TemplateSeatRepository templateSeatRepository;

    @InjectMocks
    private SessionSeatService sessionSeatService;

    private Long sessionId;

    @BeforeEach
    void setUp() {
        sessionId = 1L;
    }

    @Test
    void 존재하지_않는_회차는_404를_반환한다() {
        when(performanceSessionRepository.existsById(sessionId))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> sessionSeatService.getSeats(sessionId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("존재하지 않는 공연 회차입니다.", exception.getReason());

        verify(sessionSeatRepository, never()).findBySessionId(anyLong());
    }

    @Test
    void 회차는_존재하지만_좌석이_없으면_빈_목록을_반환한다() {
        when(performanceSessionRepository.existsById(sessionId))
                .thenReturn(true);

        when(sessionSeatRepository.findBySessionId(sessionId))
                .thenReturn(List.of());

        List<SessionSeatResponseDTO> result =
                sessionSeatService.getSeats(sessionId);

        assertTrue(result.isEmpty());

        verify(templateSeatRepository, never()).findByTemplateId(anyLong());
    }

    @Test
    void 회차에_좌석이_있으면_좌석_정보를_반환한다() {
        SessionSeat sessionSeat = mock(SessionSeat.class);
        TemplateSeat templateSeat = mock(TemplateSeat.class);

        when(performanceSessionRepository.existsById(sessionId))
                .thenReturn(true);

        when(sessionSeatRepository.findBySessionId(sessionId))
                .thenReturn(List.of(sessionSeat));

        when(sessionSeat.getSeatTemplateId()).thenReturn(10L);
        when(sessionSeat.getTemplateSeatId()).thenReturn(100L);
        when(sessionSeat.getId()).thenReturn(1000L);
        when(sessionSeat.getGrade()).thenReturn("VIP");
        when(sessionSeat.getStatus()).thenReturn("AVAILABLE");

        when(templateSeatRepository.findByTemplateId(10L))
                .thenReturn(List.of(templateSeat));

        when(templateSeat.getId()).thenReturn(100L);
        when(templateSeat.getSeatCode()).thenReturn("A1");
        when(templateSeat.getLabel()).thenReturn("A열 1번");
        when(templateSeat.getRowLabel()).thenReturn("A");
        when(templateSeat.getSeatNumber()).thenReturn(1);
        when(templateSeat.getX()).thenReturn(100);
        when(templateSeat.getY()).thenReturn(200);

        List<SessionSeatResponseDTO> result =
                sessionSeatService.getSeats(sessionId);

        assertEquals(1, result.size());

        SessionSeatResponseDTO seat = result.getFirst();

        assertEquals(1000L, seat.seatId());
        assertEquals("A1", seat.seatCode());
        assertEquals("A열 1번", seat.label());
        assertEquals("A", seat.rowLabel());
        assertEquals(1, seat.seatNumber());
        assertEquals("VIP", seat.grade());
        assertEquals(100, seat.x());
        assertEquals(200, seat.y());
        assertEquals("AVAILABLE", seat.status());

        verify(sessionSeatRepository).findBySessionId(sessionId);
        verify(templateSeatRepository).findByTemplateId(10L);
    }
}
