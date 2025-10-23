package com.sampoom.user.api.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupResponse {
    private Long userId;
    private String userName;
    private String email;
}
