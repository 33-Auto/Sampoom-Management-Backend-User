package com.sampoom.user.api.user.internal.dto;
import com.sampoom.user.common.entity.Workspace;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserRequest {
    @NotNull
    private Long userId;       // Auth에서 생성한 userId
    @NotNull
    private Workspace workspace;  // 근무지(대리점, 창고, 공장 등)
}