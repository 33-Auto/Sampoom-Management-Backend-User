package com.sampoom.user.api.user.dto.response;

import com.sampoom.user.common.entity.Position;
import com.sampoom.user.common.entity.Role;
import com.sampoom.user.common.entity.Workspace;
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
    private Workspace workspace;
    private String branch;
    private Position position;
    private LocalDateTime startedAt;  // 입사일
    private LocalDateTime endedAt;  // 퇴사일 (nullable)
}
