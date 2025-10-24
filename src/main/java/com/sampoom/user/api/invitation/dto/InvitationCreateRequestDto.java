package com.sampoom.user.api.invitation.dto;

import com.sampoom.user.api.invitation.entity.TargetType;
import com.sampoom.user.common.entity.Position;
import com.sampoom.user.common.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvitationCreateRequestDto {

    @NotNull
    private TargetType targetType;   // FACTORY / AGENCY / WAREHOUSE

    @NotNull
    private Long targetId;           // 조직 ID

    @NotBlank
    @Email
    private String email;            // 초대 대상 이메일

    @NotNull
    private Role role;               // 역할 (예: OPERATOR / MANAGER 등)

    @NotNull
    private Position position;       // 직급 (예: STAFF / SUPERVISOR 등)
}