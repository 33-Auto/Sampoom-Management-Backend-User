package com.sampoom.user.api.user.controller;


import com.sampoom.user.api.user.internal.dto.AuthUserProfile;
import com.sampoom.user.common.response.ApiResponse;
import com.sampoom.user.common.response.SuccessStatus;
import com.sampoom.user.api.user.dto.request.UserUpdateRequest;
import com.sampoom.user.api.user.dto.response.UserUpdateResponse;
import com.sampoom.user.api.user.service.UserService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Auth 통신용(Feign)
    @Hidden
    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> createProfile(@RequestBody AuthUserProfile req) {
        userService.createProfile(req);
        return ApiResponse.success_only(SuccessStatus.CREATED);
    }

    // AccessToken 내 userId로 profile 조회
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<AuthUserProfile>> getProfile(Authentication authentication){
        Long userId = Long.valueOf(authentication.getName());
        AuthUserProfile profile = userService.getProfile(userId);
        return ApiResponse.success(SuccessStatus.OK, profile);

    }

    // 회원 수정
    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<UserUpdateResponse>> patchUser(
            Authentication authentication,
            @RequestBody UserUpdateRequest reqs
    ) {
        Long userId = Long.valueOf(authentication.getName()); // Access Token에서 추출됨
        UserUpdateResponse resp = userService.updatePartialUser(userId,reqs);
        return ApiResponse.success(SuccessStatus.OK, resp);
    }

}
