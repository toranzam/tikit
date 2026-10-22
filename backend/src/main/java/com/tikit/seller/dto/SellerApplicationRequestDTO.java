package com.tikit.seller.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class SellerApplicationRequestDTO {

    private String businessName;
    private String contactEmail;
    private String description;
    private List<String> evidenceUrls;

}
