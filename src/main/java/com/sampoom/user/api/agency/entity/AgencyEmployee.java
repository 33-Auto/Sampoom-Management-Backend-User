package com.sampoom.user.api.agency.entity;

import com.sampoom.user.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "agency_employee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgencyEmployee extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long agencyId;

    @Enumerated(EnumType.STRING)
    private Position position;

    @Enumerated(EnumType.STRING)
    private EmploymentStatus status;

    @Enumerated(EnumType.STRING)
    private Role role;

    private OffsetDateTime startedAt;
    private OffsetDateTime endedAt;

    private Boolean deleted = false;
}

