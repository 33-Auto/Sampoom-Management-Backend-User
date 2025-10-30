package com.sampoom.user.api.user.controller;


import com.sampoom.user.api.user.internal.dto.SignupUser;
import com.sampoom.user.common.response.ApiResponse;
import com.sampoom.user.common.response.SuccessStatus;
import com.sampoom.user.api.user.dto.request.UserUpdateRequest;
import com.sampoom.user.api.user.dto.response.UserUpdateResponse;
import com.sampoom.user.api.user.service.UserService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Auth 통신용(Feign)
    @Hidden
    @PostMapping("/internal/profile")
    @PreAuthorize("hasAuthority('SVC_AUTH')")   // 내부 통신용 헤더
    public ResponseEntity<ApiResponse<Void>> createProfile(@Valid @RequestBody SignupUser req) {
        userService.createProfile(req);
        return ApiResponse.success_only(SuccessStatus.CREATED);
    }

    // AccessToken 내 userId로 profile 조회
    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('ROLE_MEMBER')")    // 내부 통신용 헤더 때문에 명시적 작성
    public ResponseEntity<ApiResponse<SignupUser>> getProfile(Authentication authentication){
        try {
            String name = authentication.getName();
            Long userId = Long.valueOf(name);
            if (userId <= 0) {
                throw new IllegalArgumentException("유효하지 않은 userId: " + userId);
            }
            SignupUser profile = userService.getProfile(userId);
            return ApiResponse.success(SuccessStatus.OK, profile);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("토큰 내 유효하지 않은 userId 포맷", e);
        }

    }

    // 회원 수정
    @PatchMapping("/profile")
    @PreAuthorize("hasAuthority('ROLE_MEMBER')")    // 내부 통신용 헤더 때문에 명시적 작성
    public ResponseEntity<ApiResponse<UserUpdateResponse>> patchUser(
            Authentication authentication,
            @RequestBody UserUpdateRequest reqs
    ) {
        Long userId = Long.valueOf(authentication.getName()); // Access Token에서 추출됨
        UserUpdateResponse resp = userService.updatePartialUser(userId,reqs);
        return ApiResponse.success(SuccessStatus.OK, resp);
    }

}
