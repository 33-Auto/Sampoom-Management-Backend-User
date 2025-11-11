package com.sampoom.user.api.user.dto.response;

import com.sampoom.user.common.entity.Position;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserUpdateAdminResponse {
    private Long userId;
    private String userName;
    private Position position;
}
