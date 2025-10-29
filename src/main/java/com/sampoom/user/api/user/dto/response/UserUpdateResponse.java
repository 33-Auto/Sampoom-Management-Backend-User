package com.sampoom.user.api.user.dto.response;

import com.sampoom.user.api.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserUpdateResponse {
    private Long userId;
    private String userName;
    private String position;
    private String workspace;
    private String branch;

    public static UserUpdateResponse from(User user) {
        return UserUpdateResponse.builder()
                .userId(user.getId())
                .userName(user.getUserName())
//                .position(user.getPosition())
//                .workspace(user.getWorkspace())
//                .branch(user.getBranch())
                .build();
    }
}
