package com.tikit.seller.repository;

import com.tikit.seller.entity.SellerApplication;
import com.tikit.seller.entity.SellerApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SellerApplicationRepository extends JpaRepository<SellerApplication,Long> {

    //중복신청검사용 - 회원에게 해당 상태의 신청이 있는지
    @Query("""
            select count(a)>0 
            from SellerApplication a
            where a.member.id = :memberId
                        and a.status = :status
            """)
    boolean hasStatus(@Param("memberId") Long memberId, @Param("status") SellerApplicationStatus status);

    //내 신청결과 조회 - 회원의 가장 최근 신청
    @Query("""
            select a
            from SellerApplication a
            where a.member.id = :memberId
            order by a.createdAt desc 
            limit 1
            """)
    Optional<SellerApplication> findLatest(@Param("memberId") Long memberId);
}
