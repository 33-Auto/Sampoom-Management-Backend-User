package com.sampoom.user.api.auth.entity;

import com.sampoom.user.common.entity.BaseTimeEntity;
import com.sampoom.user.common.entity.Role;
import com.sampoom.user.common.entity.Workspace;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "auth_user_projection")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AuthUserProjection extends BaseTimeEntity {
    @Id
    private Long userId;

    private String email;

    @Enumerated(EnumType.STRING)
    private Workspace workspace;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String lastEventId;

    @Column(nullable=false)
    private Long version;

    private OffsetDateTime sourceUpdatedAt;
}
