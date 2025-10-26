package com.sampoom.user.api.factory.entity;

import com.sampoom.user.api.factory.event.FactoryEvent;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "factory_projection",
        uniqueConstraints = @UniqueConstraint(name="uk_factory_projection_factory_id", columnNames = "factory_id"))
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@SQLRestriction("deleted = false")
public class FactoryProjection {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="factory_id", nullable=false)
    private Long factoryId;              // 원본 PK

    @Column(nullable=false)
    private String name;

    @Column(nullable=false)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private FactoryStatus status;        // ACTIVE/INACTIVE 등

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

    public FactoryProjection updateFromEvent(FactoryEvent e) {
        FactoryEvent.Payload p = e.getPayload();
        return this.toBuilder()
                .name(p.getName())
                .address(p.getAddress())
                .status(FactoryStatus.valueOf(p.getStatus()))
                .version(e.getVersion())
                .lastEventId(UUID.fromString(e.getEventId()))
                .deleted(e.getPayload().getDeleted())
                .sourceUpdatedAt(OffsetDateTime.parse(e.getOccurredAt()))
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    public FactoryProjection markDeleted(FactoryEvent e) {
        return this.toBuilder()
                .version(e.getVersion())
                .lastEventId(UUID.fromString(e.getEventId()))
                .deleted(true)
                .sourceUpdatedAt(OffsetDateTime.parse(e.getOccurredAt()))
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @PrePersist
    void prePersist() {
        if (updatedAt == null) updatedAt = OffsetDateTime.now();
        if (version == null) version = 0L;
        if (deleted == null) deleted = false;
    }

    @PreUpdate
    void preUpdate() { updatedAt = OffsetDateTime.now(); }
}