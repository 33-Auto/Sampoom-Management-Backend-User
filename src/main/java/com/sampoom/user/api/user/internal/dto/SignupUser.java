package com.sampoom.user.api.user.internal.dto;

import com.sampoom.user.common.entity.Position;
import com.sampoom.user.common.entity.Workspace;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NotNull
public class SignupUser {
    private Long userId;            // Auth에서 생성한 userId
    private String userName;        // 사용자 이름
    private Workspace workspace;              // 부서
    private String branch;          // 지점명
    private Position position;      // 직책
}