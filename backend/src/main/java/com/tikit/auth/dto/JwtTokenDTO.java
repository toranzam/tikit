package com.tikit.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtTokenDTO {
    private final String accessToken;
    private final String refreshToken;
}
