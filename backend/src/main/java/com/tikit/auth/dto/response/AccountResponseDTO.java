package com.tikit.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponseDTO {
    private Long accountId;
    private String email;
    private String realName;
    private String roleName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
