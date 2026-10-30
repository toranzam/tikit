package com.tikit.admin.dto.response;

import com.tikit.auth.entity.Member;
import com.tikit.auth.entity.MemberStatus;
import com.tikit.auth.entity.Role;

import java.time.Instant;

public record AdminMemberSummaryResponseDTO(
        Long id,
        String email,
        String name,
        Role role,
        MemberStatus status,
        Instant createdAt
) {
    public static AdminMemberSummaryResponseDTO from(Member member) {
        return new AdminMemberSummaryResponseDTO(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getRole(),
                member.getStatus(),
                member.getCreatedAt()
        );
    }
}
