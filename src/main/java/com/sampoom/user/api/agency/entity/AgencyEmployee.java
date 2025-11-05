package com.sampoom.user.api.agency.entity;

import com.sampoom.user.common.entity.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "agency_employee")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SQLDelete(sql = "UPDATE agency_employee SET deleted = true, updated_at = now() WHERE agency_employee_id = ?")
@SQLRestriction("deleted = false")
@Builder
public class AgencyEmployee extends BaseEmployeeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agency_employee_id")
    private Long id;  // 대리점-직원 ID

    @Column(nullable = false)
    private Long agencyId;  // 대리점 ID
}
