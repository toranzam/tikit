package com.tikit.admin.service;

import com.tikit.admin.dto.request.AdminMemberSearchRequestDTO;
import com.tikit.admin.dto.response.AdminMemberDetailResponseDTO;
import com.tikit.admin.dto.response.AdminMemberListResponseDTO;
import com.tikit.admin.exception.AdminMemberNotFoundException;
import com.tikit.admin.repository.AdminMemberSpecifications;
import com.tikit.auth.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberService {

    private final MemberRepository memberRepository;

    public AdminMemberListResponseDTO getMembers(AdminMemberSearchRequestDTO request) {
        return AdminMemberListResponseDTO.from(memberRepository.findAll(
                AdminMemberSpecifications.search(request.getKeyword(), request.getStatus(), request.getRole()),
                request.toPageable()
        ));
    }

    public AdminMemberDetailResponseDTO getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .map(AdminMemberDetailResponseDTO::from)
                .orElseThrow(AdminMemberNotFoundException::new);
    }
}
