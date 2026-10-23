
package com.tikit.seat.service;

import com.tikit.seat.dto.response.SessionSeatResponseDTO;
import com.tikit.seat.entity.SessionSeat;
import com.tikit.seat.entity.TemplateSeat;
import com.tikit.seat.repository.PerformanceSessionRepository;
import com.tikit.seat.repository.SessionSeatRepository;
import com.tikit.seat.repository.TemplateSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionSeatService {

    private final PerformanceSessionRepository performanceSessionRepository;
    private final SessionSeatRepository sessionSeatRepository;
    private final TemplateSeatRepository templateSeatRepository;

    @Transactional(readOnly = true)
    public List<SessionSeatResponseDTO> getSeats(Long sessionId) {

        if (!performanceSessionRepository.existsById(sessionId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "존재하지 않는 공연 회차입니다."
            );
        }

        List<SessionSeat> sessionSeats =
                sessionSeatRepository.findBySessionId(sessionId);

        if (sessionSeats.isEmpty()) {
            return List.of();
        }

        Long templateId = sessionSeats.getFirst().getSeatTemplateId();

        Map<Long, TemplateSeat> templateSeatMap =
                templateSeatRepository.findByTemplateId(templateId)
                        .stream()
                        .collect(Collectors.toMap(
                                TemplateSeat::getId,
                                Function.identity()
                        ));

        return sessionSeats.stream()
                .map(seat -> {
                    TemplateSeat templateSeat =
                            templateSeatMap.get(seat.getTemplateSeatId());

                    return new SessionSeatResponseDTO(
                            seat.getId(),
                            templateSeat.getSeatCode(),
                            templateSeat.getLabel(),
                            templateSeat.getRowLabel(),
                            templateSeat.getSeatNumber(),
                            seat.getGrade(),
                            templateSeat.getX(),
                            templateSeat.getY(),
                            seat.getStatus()
                    );
                })
                .toList();
    }
}
