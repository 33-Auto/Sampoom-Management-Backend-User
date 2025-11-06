package com.sampoom.user.api.agency.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "agency_projection",
        uniqueConstraints = @UniqueConstraint(name = "uk_agency_projection_agency_id", columnNames = "agency_id"),
        indexes = {
                @Index(name = "idx_agency_projection_status", columnList = "status"),
                @Index(name = "idx_agency_projection_deleted", columnList = "deleted")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@SQLRestriction("deleted = false")
public class AgencyProjection {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agency_id", nullable = false)
    private Long agencyId;          // 원본 PK

    @Column(nullable = false, unique = true, length = 20)
    private String agencyCode;          // 원본 PK

    @Column(nullable = false, length = 100)
    private String agencyName;

    private String ceoName;
    private String address;

    private Double latitude;
    private Double longitude;

    private String businessNumber;

    @Enumerated(EnumType.STRING)
    private VendorStatus status;          // ACTIVE / INACTIVE

    @Builder.Default
    @Column(nullable = false)
    private Boolean deleted = false;

    @Column(nullable = false)
    private Long version;

    @Column(name = "last_event_id")
    private UUID lastEventId;

    private OffsetDateTime sourceUpdatedAt;
    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        if (updatedAt == null) updatedAt = OffsetDateTime.now();
        if (version == null) version = 0L;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    // 거래처 비활성화
    public void deactivate() {
        this.status = VendorStatus.INACTIVE;
    }
}
