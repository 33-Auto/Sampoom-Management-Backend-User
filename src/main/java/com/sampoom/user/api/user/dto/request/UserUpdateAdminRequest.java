package com.sampoom.user.api.user.dto.request;

import com.sampoom.user.common.entity.Position;
import com.sampoom.user.common.entity.Workspace;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateAdminRequest {
    private Long userId;
    private Workspace workspace;
    private Position position;
}
