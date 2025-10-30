package com.sampoom.user.api.factory.entity;

import com.sampoom.user.common.entity.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

import static com.sampoom.user.common.entity.EmployeeStatus.ACTIVE;

@Entity
@Table(name = "factory_employee")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE factory_employee SET updated_at = now() WHERE factory_employee_id = ?")
@SQLRestriction("deleted = false")
public class FactoryEmployee extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "factory_employee_id")
    private Long id;  // 공장-직원 ID

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
    private Long factoryId;  // 공장 ID

    @PrePersist
    void prePersist() {
        if (status == null) status = ACTIVE;
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