package com.sampoom.user.api.user.controller;

import com.sampoom.user.api.user.dto.response.UserInfoListResponse;
import com.sampoom.user.api.user.dto.response.UserInfoResponse;
import com.sampoom.user.api.user.internal.dto.LoginUserRequest;
import com.sampoom.user.api.user.internal.dto.LoginUserResponse;
import com.sampoom.user.api.user.internal.dto.SignupUser;
import com.sampoom.user.api.user.service.UserInfoService;
import com.sampoom.user.api.user.service.UserService;
import com.sampoom.user.common.entity.Workspace;
import com.sampoom.user.common.response.ApiResponse;
import com.sampoom.user.common.response.SuccessStatus;
import com.sampoom.user.api.user.dto.request.UserUpdateRequest;
import com.sampoom.user.api.user.dto.response.UserUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name="USER", description = "User 관련 API 입니다.<br>로그인한 유저만 가능합니다.")
public class UserController {

    private final UserService userService;
    private final UserInfoService userInfoService;

    // Auth 통신용(Feign)
    @Operation(summary = "[Not Client API] 회원가입 User 서비스 내부 통신용", description = "[Not Client API] 회원가입을 통해 프로필 정보를 담은 유저를 생성합니다.")
    @PostMapping("/internal/profile")
    @PreAuthorize("hasAuthority('SVC_AUTH')")   // 내부 통신용 헤더
    public ResponseEntity<Void> createProfile(@Valid @RequestBody SignupUser req) {
        userService.createProfile(req);
        return ResponseEntity.ok().build();
    }

    // Auth 통신용(Feign)
    @Operation(summary = "[Not Client API] 로그인 User 서비스 내부 통신용", description = "[Not Client API] 로그인을 통해 유저의 조직 정합성을 검증합니다.")
    @PostMapping("/internal/verify")
    @PreAuthorize("hasAuthority('SVC_AUTH')")   // 내부 통신용 헤더
    public ResponseEntity<LoginUserResponse> verifyWorkspace(@Valid @RequestBody LoginUserRequest req) {
        LoginUserResponse res = userService.verifyWorkspace(req);
        return ResponseEntity.ok(res);
    }

    // AccessToken 내 userId로 profile 조회
    @Operation(summary = "로그인 유저 프로필 정보 조회", description = "토큰으로 로그인한 유저의 프로필 정보를 조회합니다.")
    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('ROLE_USER')")    // 내부 통신용 헤더 때문에 명시적 작성
    public ResponseEntity<ApiResponse<UserInfoResponse>> getMyProfile(
            @Valid @RequestBody Workspace workspace,
            Authentication authentication
    ){
        try {
            String name = authentication.getName();
            Long userId = Long.valueOf(name);
            if (userId <= 0) {
                throw new IllegalArgumentException("유효하지 않은 userId: " + userId);
            }
            UserInfoResponse profile = userService.getMyProfile(userId, workspace);
            return ApiResponse.success(SuccessStatus.OK, profile);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("토큰 내 유효하지 않은 userId 포맷", e);
        }

    }
    // 모든 회원의 전체 정보 조회
    @Operation(summary = "모든 회원의 전체 회원 정보를 조회", description = """
            모든 회원의 전체 정보를 페이지 형태로 불러옵니다.<br><br> page: n번째 페이지부터 불러오기 <br> size: 페이지 당 사이즈 <br> sort: 정렬기준(id, userName),정렬순서(ASC,DESC)
            """)
    @GetMapping("/info")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse<UserInfoListResponse>> getAllUsersInfo(@ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        UserInfoListResponse resp = userInfoService.getAllUsersInfo(pageable);
        return ApiResponse.success(SuccessStatus.OK, resp);
    }

    // 회원 수정
    @Operation(summary = "로그인 유저 프로필 정보 수정", description = "토큰으로 로그인한 유저의 프로필 정보를 수정합니다.")
    @PatchMapping("/profile")
    @PreAuthorize("hasAuthority('ROLE_USER')")    // 내부 통신용 헤더 때문에 명시적 작성
    public ResponseEntity<ApiResponse<UserUpdateResponse>> patchMyProfile(
            Authentication authentication,
            @RequestBody UserUpdateRequest reqs
    ) {
        Long userId = Long.valueOf(authentication.getName()); // Access Token에서 추출됨
        UserUpdateResponse resp = userService.updateMyProfile(userId,reqs);
        return ApiResponse.success(SuccessStatus.OK, resp);
    }

}
