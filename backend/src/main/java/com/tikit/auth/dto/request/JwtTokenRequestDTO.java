package com.tikit.auth.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtTokenRequestDTO {
    private String refreshToken;
}
