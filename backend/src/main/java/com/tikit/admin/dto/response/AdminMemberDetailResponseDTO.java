package com.tikit.admin.dto.response;

import com.tikit.auth.entity.Member;
import com.tikit.auth.entity.MemberStatus;
import com.tikit.auth.entity.Role;

import java.time.Instant;

public record AdminMemberDetailResponseDTO(
        Long id,
        String email,
        String name,
        Role role,
        MemberStatus status,
        Instant createdAt,
        Instant updatedAt,
        Instant withdrawnAt
) {
    public static AdminMemberDetailResponseDTO from(Member member) {
        return new AdminMemberDetailResponseDTO(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getRole(),
                member.getStatus(),
                member.getCreatedAt(),
                member.getUpdatedAt(),
                member.getWithdrawnAt()
        );
    }
}
