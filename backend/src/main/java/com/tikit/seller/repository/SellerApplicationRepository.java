package com.tikit.seller.repository;

import com.tikit.seller.entity.SellerApplication;
import com.tikit.seller.entity.SellerApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerApplicationRepository extends JpaRepository<SellerApplication,Long> {

    //중복신청검사용 - 회원에게 해당 상태의 신청이 있는지
    boolean existsByMemberIdAndStatus(Long memberId, SellerApplicationStatus status);

    //내 신청결과 조회 - 회원의 가장 최근 신청
    Optional<SellerApplication> findFirstByMemberIdOrderByCreatedAtDesc(Long memberId);
}
