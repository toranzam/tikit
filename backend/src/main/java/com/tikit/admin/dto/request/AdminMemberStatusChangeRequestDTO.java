package com.tikit.admin.dto.request;

import com.tikit.auth.entity.MemberStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminMemberStatusChangeRequestDTO(
        @NotNull(message = "변경할 회원 상태는 필수입니다.")
        MemberStatus status,
        @NotBlank(message = "상태 변경 사유는 필수입니다.")
        @Size(max = 1000, message = "상태 변경 사유는 1000자 이내로 입력해야 합니다.")
        String reason
) {
}
