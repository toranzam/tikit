package com.tikit.booking.repository;

import com.tikit.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // 본인 예매만 조회 (다른 회원 예매는 없는 예매와 같게 처리)
    Optional<Booking> findByIdAndMemberId(Long id, Long memberId);
}
