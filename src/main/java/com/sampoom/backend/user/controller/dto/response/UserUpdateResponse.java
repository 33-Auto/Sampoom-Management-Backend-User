package com.sampoom.backend.user.controller.dto.response;

import com.sampoom.backend.user.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserUpdateResponse {
    private Long userId;
    private String email;
    private String userName;
    private String position;
    private String workspace;
    private String branch;

    public static UserUpdateResponse from(User user) {
        return UserUpdateResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .userName(user.getUserName())
                .position(user.getPosition())
                .workspace(user.getWorkspace())
                .branch(user.getBranch())
                .build();
    }
}
