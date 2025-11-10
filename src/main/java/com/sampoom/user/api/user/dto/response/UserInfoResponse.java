package com.sampoom.user.api.user.dto.response;

import com.sampoom.user.common.entity.EmployeeStatus;
import com.sampoom.user.common.entity.Position;
import com.sampoom.user.common.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfoResponse {
    private Long userId;

    // AuthUserProjection
    private String email;
    private Role role;
    // User
    private String userName;
    // Employee
    private Long organizationId;
    private String branch;

    private Position position;

    private EmployeeStatus status;

    private LocalDateTime createdAt;    // 최초 입사일
    private LocalDateTime startedAt;    // 근무 시작일
    private LocalDateTime endedAt;      // 근무 종료일
    private LocalDateTime deletedAt;    // 퇴사일
}
