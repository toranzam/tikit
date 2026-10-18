package com.tikit.auth.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountRegisterRequestDTO {
    private String email;
    private String rawPassword;
    private String realName;
}
