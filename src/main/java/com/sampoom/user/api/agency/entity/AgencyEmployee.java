package com.sampoom.user.api.agency.entity;

import com.sampoom.user.common.entity.*;
import jakarta.persistence.*;
import lombok.*;

import static com.sampoom.user.common.entity.Workspace.AGENCY;

@Entity
@Table(name = "agency_employee")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AgencyEmployee extends BaseMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agency_employee_id")
    private Long id;  // 대리점-직원 ID

    @Column(nullable = false)
    private Long agencyId;  // 대리점 ID
}
