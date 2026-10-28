
package com.tikit.seat.repository;

import com.tikit.seat.entity.SessionSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionSeatRepository extends JpaRepository<SessionSeat, Long> {

    List<SessionSeat> findBySessionId(Long sessionId);
}
