package com.sampoom.user.api.user.controller;


import com.sampoom.user.common.response.ApiResponse;
import com.sampoom.user.common.response.SuccessStatus;
import com.sampoom.user.api.user.dto.request.SignupRequest;
import com.sampoom.user.api.user.dto.request.UserUpdateRequest;
import com.sampoom.user.api.user.dto.request.VerifyLoginRequest;
import com.sampoom.user.api.user.dto.response.UserUpdateResponse;
import com.sampoom.user.api.user.external.dto.UserResponse;
import com.sampoom.user.api.user.dto.response.SignupResponse;
import com.sampoom.user.api.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest req) {
        SignupResponse resp = userService.signup(req);
        return ApiResponse.success(SuccessStatus.CREATED, resp);
    }

    // 회원 수정
    @PatchMapping("/update")
    public ResponseEntity<ApiResponse<UserUpdateResponse>> patchUser(
            Authentication authentication,
            @RequestBody UserUpdateRequest reqs
    ) {
        Long userId = Long.valueOf(authentication.getName()); // Access Token에서 추출됨
        UserUpdateResponse resp = userService.updatePartialUser(userId,reqs);
        return ApiResponse.success(SuccessStatus.OK, resp);
    }

    @PostMapping("/verify")
    public ApiResponse<UserResponse> verifyLogin(@RequestBody VerifyLoginRequest req) {
        UserResponse resp = userService.verifyLogin(req);
        return ApiResponse.success_msa(SuccessStatus.OK,resp);
    }

}
