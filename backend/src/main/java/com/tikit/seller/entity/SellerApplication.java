package com.tikit.seller.entity;

import com.tikit.auth.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seller_applications")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SellerApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "business_name", nullable = false, length = 100)
    private String businessName;

    @Column(name="contact_email", nullable = false, length = 254)
    private String contactEmail;

    @Column(name="description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="evidence_urls", nullable = false, columnDefinition = "jsonb")
    private List<String> evidenceUrls = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable = false, length = 20)
    private SellerApplicationStatus status;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private Member reviewerId;

    @Column(name = "review_reason", columnDefinition = "TEXT")
    private String reviewReason; //거절사유

    @Column(name = "reviewed_at")
    private Instant reviewedAt;//심사 시각

    @CreatedDate
    @Column(name="created_at", updatable = false, nullable = false)
    private Instant createdAt;


    @LastModifiedDate
    @Column(name="updated_at", nullable = false)
    private Instant updatedAt;


    public static SellerApplication create(Member member, String businessName,
                                           String contactEmail, String description,
                                           List<String> evidenceUrls){
        SellerApplication a = new SellerApplication();
        a.member = member;
        a.businessName = businessName;
        a.contactEmail = contactEmail;
        a.description = description;
        a.evidenceUrls = (evidenceUrls == null) ? new ArrayList<>() : new ArrayList<>(evidenceUrls);
        a.status = SellerApplicationStatus.PENDING;
        return a;
    }

}
