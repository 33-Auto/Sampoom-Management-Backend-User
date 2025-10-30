package com.sampoom.user.api.agency.entity;

import com.sampoom.user.common.entity.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

import static com.sampoom.user.common.entity.EmployeeStatus.ACTIVE;

@Entity
@Table(name = "agency_employee")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE agency_employee SET deleted = true, updated_at = now() WHERE agency_employee_id = ?")
@SQLRestriction("deleted = false")
public class AgencyEmployee extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agency_employee_id")
    private Long id;  // 대리점-직원 ID

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Position position;  // 직급

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmployeeStatus status;  // 상태 (근무중, 퇴사 등)

    @Column(nullable = false)
    private LocalDateTime startedAt;  // 입사일

    private LocalDateTime endedAt;  // 퇴사일 (nullable)

    @Column(nullable = false)
    private Long userId;  // 직원 ID

    @Column(nullable = false)
    private Long agencyId;  // 대리점 ID

    @PrePersist
    void prePersist() {
        if (status == null) status = ACTIVE ;
        if (startedAt == null) startedAt = LocalDateTime.now();
    }

    public void terminate() {
        this.status = EmployeeStatus.RETIRED;
        this.endedAt = LocalDateTime.now();
    }

    public void updatePosition(Position newPosition) {
        this.position = newPosition;
    }
}
