package com.sampoom.user.api.user.dto.response;

import com.sampoom.user.api.user.entity.User;
import com.sampoom.user.common.entity.Position;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserUpdateResponse {
    private Long userId;
    private String userName;
    private Position position;
    private String workspace;
    private String branch;

    public static UserUpdateResponse from(User user) {
        return UserUpdateResponse.builder()
                .userId(user.getId())
                .userName(user.getUserName())
                .build();
    }
}
