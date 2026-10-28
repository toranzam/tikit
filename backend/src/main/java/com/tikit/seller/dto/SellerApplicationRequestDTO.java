package com.tikit.seller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class SellerApplicationRequestDTO {

    @NotBlank(message = "사업자명(업체명)을 입력하세요")
    @Size(max = 100)
    private String businessName;

    @NotBlank(message = "이메일을 입력하세요")
    @Email
    private String contactEmail;

    @NotBlank(message = "신청사유를 입력하세요")
    @Size(max = 2000)
    private String description;

    @NotEmpty(message = "자료를 입력하세요")
    private List<@NotBlank String> evidenceUrls;

}
