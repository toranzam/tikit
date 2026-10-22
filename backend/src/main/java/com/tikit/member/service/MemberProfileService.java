package com.tikit.member.service;

import com.tikit.auth.entity.Member;
import com.tikit.auth.repository.AccountRepository;
import com.tikit.member.dto.MemberProfileResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 회원 엔티티 조회 및 응답 DTO 변환

@Service
@RequiredArgsConstructor
public class MemberProfileService {

    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public MemberProfileResponseDTO getMyProfile(Long memberId) {

        Member member = accountRepository.findById(memberId)

                // 해당 회원이 존재하지 않으면 예외 발생
                .orElseThrow(() ->
                        new IllegalArgumentException("회원을 찾을 수 없습니다.")
                );

        return MemberProfileResponseDTO.from(member);
    }
}