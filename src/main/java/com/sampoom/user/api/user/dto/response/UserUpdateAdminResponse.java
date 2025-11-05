package com.sampoom.user.api.user.dto.response;

import com.sampoom.user.common.entity.Position;
import lombok.Getter;

@Getter
public class UserUpdateAdminResponse {
    private Long userId;
    private Position position;
}
