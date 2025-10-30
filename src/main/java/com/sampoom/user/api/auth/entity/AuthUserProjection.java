package com.sampoom.user.api.auth.entity;

import com.sampoom.user.common.entity.BaseTimeEntity;
import com.sampoom.user.common.entity.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "auth_user_projection")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AuthUserProjection extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String lastEventId;
    private Long version;

    public void updateFromPayload(Role role) {
        this.role = role;
    }
}
