package com.sampoom.user.api.user.internal.dto;

import com.sampoom.user.common.entity.Position;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupUser {
    @NotNull
    private Long userId;       // Auth에서 생성한 userId
    @NotBlank
    private String userName;   // 사용자 이름
    private String workspace;  // 근무지(대리점, 창고, 공장 등)
    private String branch;     // 지점명
    private Position position;   // 직책
}