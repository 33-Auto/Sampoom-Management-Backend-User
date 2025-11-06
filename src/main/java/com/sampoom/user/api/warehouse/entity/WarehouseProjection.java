package com.sampoom.user.api.warehouse.entity;

import com.sampoom.user.common.entity.BranchStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "warehouse_projection",
        uniqueConstraints = @UniqueConstraint(name="uk_warehouse_projection_warehouse_id", columnNames = "warehouse_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@SQLRestriction("deleted = false")
public class WarehouseProjection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="warehouse_id", nullable=false)
    private Long warehouseId;              // 원본 PK

    @Column(nullable = false, unique = true, length = 20)
    private String branchCode;

    @Column(nullable=false)
    private String name;

    private String address;

    private Double latitude;
    private Double longitude;

    @Enumerated(EnumType.STRING)
    private BranchStatus status;        // ACTIVE/INACTIVE 등

    @Builder.Default
    @Column(nullable=false)
    private Boolean deleted = false;

    // --- 동기화 안전 메타(둘 중 하나 이상 필수) ---
    @Column(nullable=false)
    private Long version;

    @Column(name="last_event_id", columnDefinition="uuid")
    private UUID lastEventId;

    // 시간은 타 서비스와 교환되므로 OffsetDateTime 권장
    private OffsetDateTime sourceUpdatedAt;  // 원본 updatedAt

    @Column(nullable=false)
    private OffsetDateTime updatedAt;        // 프로젝션 갱신 시각

    @PrePersist
    void onCreate() {
        if (updatedAt == null) updatedAt = OffsetDateTime.now();
        if (version == null) version = 0L;
        if (deleted == null) deleted = false;
    }

    @PreUpdate
    void onUpdate() { updatedAt = OffsetDateTime.now(); }

    public void deactivate() {
        this.status = BranchStatus.INACTIVE;
    }
}
