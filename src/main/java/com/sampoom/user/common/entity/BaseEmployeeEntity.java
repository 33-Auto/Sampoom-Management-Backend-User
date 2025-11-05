package com.sampoom.user.common.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import static com.sampoom.user.common.entity.EmployeeStatus.ACTIVE;

@MappedSuperclass
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseEmployeeEntity extends SoftDeleteEntity {

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

    @PrePersist
    void prePersist() {
        if (status == null) status = ACTIVE;
        if (startedAt == null) startedAt = LocalDateTime.now();
    }


    public void terminate() {
        this.status = EmployeeStatus.RETIRED;
        this.endedAt = LocalDateTime.now();
    }

    // setter 추가
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    // 더티 체킹
    public void updatePosition(Position newPosition) {
        this.position = newPosition;
    }
}
