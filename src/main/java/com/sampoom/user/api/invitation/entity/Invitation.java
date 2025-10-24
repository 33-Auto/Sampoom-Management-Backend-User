package com.sampoom.user.api.invitation.entity;

import com.sampoom.user.common.entity.Position;
import com.sampoom.user.common.entity.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "invitation")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String inviteCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TargetType targetType; // FACTORY, AGENCY, WAREHOUSE

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false, length = 128)
    private String emailHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Position position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private InvitationStatus status;


    @PrePersist
    void prePersist() {
        if (status == null) status = InvitationStatus.PENDING;
    }

    public void accept() { this.status = InvitationStatus.ACCEPTED; }
}