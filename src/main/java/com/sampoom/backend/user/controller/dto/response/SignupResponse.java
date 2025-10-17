package com.sampoom.backend.user.controller.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupResponse {
    private Long userId;
    private String userName;
    private String email;
}
