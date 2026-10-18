package com.tikit.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountLoginResponseDTO {
    private final String accessToken;
    private final String refreshToken;
}
