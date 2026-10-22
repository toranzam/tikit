package com.tikit.seller.dto;

import com.tikit.seller.entity.SellerApplication;
import com.tikit.seller.entity.SellerApplicationStatus;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
public class SellerApplicationResponseDTO {

    private final Long id;
    private final SellerApplicationStatus status;
    private final String businessName;
    private final String contactEmail;
    private final String description;
    private final List<String> evidenceUrls;
    private final String rejectReason;
    private final Instant reviewedAt;
    private final Long version;
    private final Instant createdAt;

    private SellerApplicationResponseDTO(SellerApplication a){
        this.id = a.getId();
        this.status = a.getStatus();
        this.businessName = a.getBusinessName();
        this.contactEmail = a.getContactEmail();
        this.description = a.getDescription();
        this.evidenceUrls = a.getEvidenceUrls();
        this.rejectReason = a.getRejectReason();
        this.reviewedAt = a.getReviewedAt();
        this.version = a.getVersion();
        this.createdAt = a.getCreatedAt();
    }

}
