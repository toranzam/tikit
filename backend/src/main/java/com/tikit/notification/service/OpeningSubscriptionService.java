package com.tikit.notification.service;

import com.tikit.notification.entity.OpeningSubscription;
import com.tikit.notification.entity.OpeningSubscriptionId;
import com.tikit.notification.repository.OpeningSubscriptionRepository;
import com.tikit.notification.repository.PerformanceLookupRepository;

import com.tikit.auth.entity.Member;
import com.tikit.auth.repository.AccountRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;


// 공연별 예매 시작 알림 신청 관리
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OpeningSubscriptionService {

    private final OpeningSubscriptionRepository subscriptionRepository;
    private final PerformanceLookupRepository performanceLookupRepository;
    private final AccountRepository accountRepository;

    // 공연별 알림 신청
    @Transactional
    public void subscribe(Long memberId, Long performanceId) {

        if (!performanceLookupRepository.existsById(performanceId)) {
            throw new NoSuchElementException("존재하지 않는 공연입니다.");
        }

        OpeningSubscriptionId id =
                new OpeningSubscriptionId(memberId, performanceId);

        if (subscriptionRepository.existsById(id)) {
            throw new IllegalStateException("이미 알림을 신청한 공연입니다.");
        }

        // 신청 저장
        OpeningSubscription subscription =
                new OpeningSubscription(memberId, performanceId);

        subscriptionRepository.save(subscription);
    }

    // 공연별 알림 신청 <<해제>>
    @Transactional
    public void unsubscribe(Long memberId, Long performanceId) {

        OpeningSubscriptionId id =
                new OpeningSubscriptionId(memberId, performanceId);

        OpeningSubscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("알림 신청 내역이 없습니다."));

        subscriptionRepository.delete(subscription);
    }

    // 회원별 알림 신청 목록 조회
    public List<OpeningSubscription> getSubscriptions(Long memberId) {
        return subscriptionRepository.findByMemberId(memberId);
    }

    // 특정 공연의 알림 신청 여부 조회
    public boolean isSubscribed(Long memberId, Long performanceId) {
        return subscriptionRepository.existsByMemberIdAndPerformanceId(
                memberId, performanceId
        );
    }

    // 전체 알림 수신 설정 조회
    public boolean getNotificationEnabled(Long memberId) {

        Member member = accountRepository.findById(memberId)
                .orElseThrow(() ->
                        new NoSuchElementException("회원을 찾을 수 없습니다."));

        return member.isNotificationEnabled();
    }

    // 전체 알림 수신 설정 <<변경>>
    @Transactional
    public boolean updateNotificationEnabled(Long memberId, boolean enabled) {

        Member member = accountRepository.findById(memberId)
                .orElseThrow(() ->
                        new NoSuchElementException("회원을 찾을 수 없습니다."));

        member.changeNotificationEnabled(enabled);

        return member.isNotificationEnabled();
    }

}
