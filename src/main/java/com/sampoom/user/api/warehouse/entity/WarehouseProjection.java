package com.sampoom.user.api.warehouse.entity;

import com.sampoom.user.api.factory.entity.FactoryStatus;
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

    @Column(nullable=false)
    private String name;

    @Column(nullable=false)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private WarehouseStatus status;        // ACTIVE/INACTIVE 등

    // --- 동기화 안전 메타(둘 중 하나 이상 필수) ---
    @Column(nullable=false)
    private Long version;

    @Column(name="last_event_id", columnDefinition="uuid")
    private UUID lastEventId;

    @Builder.Default
    @Column(nullable=false)
    private Boolean deleted = false;

    // 시간은 타 서비스와 교환되므로 OffsetDateTime 권장
    private OffsetDateTime sourceUpdatedAt;  // 원본 updatedAt

    @Column(nullable=false)
    private OffsetDateTime updatedAt;        // 프로젝션 갱신 시각
}
