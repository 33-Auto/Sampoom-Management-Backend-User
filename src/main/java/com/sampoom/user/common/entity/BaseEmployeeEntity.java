package com.sampoom.user.common.entity;

import com.sampoom.user.common.exception.BadRequestException;
import com.sampoom.user.common.response.ErrorStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseEmployeeEntity extends SoftDeleteEntity {

    @Enumerated(EnumType.STRING)
    private Position position;  // 직급

    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;  // 상태 (근무중, 퇴사 등)

    @Column(nullable = false)
    private LocalDateTime startedAt;  // 근무시작일 (휴직 포함)

    private LocalDateTime endedAt;  // 근무종료일 (휴직 포함)

    @Column(nullable = false)
    private Long userId;  // 직원 ID

    @Version
    @Column(nullable = false)
    private Long version; // 낙관적 락 & 이벤트 버전 관리

    @PrePersist
    void prePersist() {
        if (version == null) version = 0L;
        if (status == null) status = EmployeeStatus.ACTIVE;
        if (startedAt == null) startedAt = LocalDateTime.now();
    }

    public void onUpdateStatus(EmployeeStatus employeeStatus){
        if (this.status == employeeStatus) return;
        this.status = employeeStatus;
        switch (employeeStatus){
            case RETIRED -> {
                this.endedAt = LocalDateTime.now();
                softDelete();
            }
            case ACTIVE -> {
                this.startedAt = LocalDateTime.now();
                this.endedAt = null;
                reactivation();
            }
            case LEAVE -> this.endedAt = LocalDateTime.now();
            default -> throw new BadRequestException(ErrorStatus.INVALID_EMPSTATUS_TYPE);
        }
    }

    // setter 추가
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    // 더티 체킹
    public void updatePosition(Position newPosition) {
        this.position = newPosition;
    }
}
