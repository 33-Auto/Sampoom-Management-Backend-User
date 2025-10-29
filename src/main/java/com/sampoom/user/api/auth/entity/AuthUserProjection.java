package com.sampoom.user.api.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "auth_user_projection",
        uniqueConstraints = @UniqueConstraint(name="uk_auth_user_projection_user_id", columnNames = "user_id")
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@SQLRestriction("deleted = false")
public class AuthUserProjection {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;            // AuthUser.id

    @Column(nullable=false, length=100)
    private String email;           // 인증용 이메일(표시용)

    @Column(nullable=false, length=30)
    private String role;            // ROLE_USER / ROLE_ADMIN ...

    @Builder.Default
    @Column(nullable=false)
    private Boolean deleted = false; // Auth isDeleted 미러링(표시용)

    // 멱등/순서 보장용 메타
    @Column(nullable=false)
    private Long version;           // 증가 버전

    @Column(name="last_event_id", columnDefinition="uuid")
    private UUID lastEventId;       // 마지막 이벤트 ID

    private OffsetDateTime sourceUpdatedAt; // Auth 원본 갱신 시각
    @Column(nullable=false)
    private OffsetDateTime updatedAt;       // 이 프로젝션 갱신 시각

    @PrePersist
    void prePersist() {
        if (updatedAt == null) updatedAt = OffsetDateTime.now();
        if (version == null) version = 0L;
        if (deleted == null) deleted = false;
    }

    @PreUpdate
    void preUpdate() { updatedAt = OffsetDateTime.now(); }
}
