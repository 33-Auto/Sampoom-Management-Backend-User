package com.sampoom.user.api.user.controller;

import com.sampoom.user.api.user.dto.request.EmployeeStatusRequest;
import com.sampoom.user.api.user.dto.request.UserUpdateAdminRequest;
import com.sampoom.user.api.user.dto.response.*;
import com.sampoom.user.api.user.internal.dto.SignupUser;
import com.sampoom.user.api.user.service.UserInfoService;
import com.sampoom.user.api.user.service.UserService;
import com.sampoom.user.common.entity.Workspace;
import com.sampoom.user.common.response.ApiResponse;
import com.sampoom.user.common.response.SuccessStatus;
import com.sampoom.user.api.user.dto.request.UserUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name="USER", description = "User 관련 API 입니다.<br>로그인한 유저만 가능합니다.")
public class UserController {

    private final UserService userService;
    private final UserInfoService userInfoService;

    // Auth 통신용(Feign)
    @Operation(summary = "[Not Client API] 회원가입 User 서비스 내부 통신용", description = "[Not Client API] 회원가입을 통해 프로필 정보를 담은 유저를 생성합니다.")
    @PostMapping("/internal/profile")
    public ResponseEntity<Void> createProfile(@Valid @RequestBody SignupUser req) {
        userService.createProfile(req);
        return ResponseEntity.ok().build();
    }

    // AccessToken 내 userId로 profile 조회
    @Operation(summary = "로그인 유저 프로필 정보 조회", description = "토큰으로 로그인한 유저의 프로필 정보를 조회합니다.")
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserLoginResponse>> getMyProfile(
            Authentication authentication
    ){
        Long userId = Long.valueOf(authentication.getName());
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        UserLoginResponse profile = userService.getMyProfile(userId, authorities);
        return ApiResponse.success(SuccessStatus.OK, profile);
    }

    // 모든 회원의 전체 정보 조회
    @Operation(summary = "조건에 맞는 회원의 전체 회원 정보를 조회", description = """
            조건에 맞는 회원의 전체 정보를 페이지 형태로 불러옵니다.
            <br><br> **정렬**
            <br> **page**: n번째 페이지부터 불러오기
            <br> **size**: 페이지 당 사이즈
            <br> **sort**: 정렬기준(id, userName),정렬순서(ASC,DESC)
            <br><br> **검색조건**
            <br> **workspace**: 권한(부서) (미지정 시 조직 상관없이 전체 조회)
            <br> **organizationId**: 조직 ID (**workspace 필수**, 미지정 시 부서 내 전체 조회)
            """)
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<UserInfoListResponse>> getUsersInfo(
            @ParameterObject
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable,
            @RequestParam(required=false) Workspace workspace,
            @RequestParam(required=false)Long organizationId
    ) {
        UserInfoListResponse resp = userInfoService.getUsersInfo(pageable, workspace, organizationId);
        return ApiResponse.success(SuccessStatus.OK, resp);
    }

    // 본인 회원 수정
    @Operation(summary = "로그인 유저 프로필 정보 수정", description = "토큰으로 로그인한 유저의 프로필 정보를 수정합니다.")
    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<UserUpdateResponse>> updateMyProfile(
            Authentication authentication,
            @RequestBody UserUpdateRequest reqs
    ) {
        Long userId = Long.valueOf(authentication.getName()); // AccessToken에서 추출됨
        UserUpdateResponse resp = userService.updateMyProfile(userId,reqs);
        return ApiResponse.success(SuccessStatus.OK, resp);
    }

    // 관리자 권한 회원 수정
    @Operation(summary = "관리자 권한 프로필 정보 수정", description = """
    관리자 권한으로 유저ID와 조직을 통해 직원의 조직 정보를 수정합니다.
    <br><br> 해당 회원의 userId 를 입력하고 알맞는 조직을 선택하세요.
    <br> 변경할 Workspace 값을 요청으로 보내세요.
    <br><br>***Workspace***
    <br>USER: 일반
    <br>ADMIN: 관리자
    """)

    @PatchMapping("/profile/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<UserUpdateAdminResponse>> updateUserProfile(
            Authentication authentication,
            @PathVariable Long userId,
            @RequestParam Workspace workspace,
            @RequestBody UserUpdateAdminRequest reqs
    ) {
        // 로깅, 감사용
        Long adminId = Long.valueOf(authentication.getName());
        log.info("관리자ID: {} 관리자가 -> 직원ID: {} 직원의 정보를 수정했습니다. ", adminId, userId);
        UserUpdateAdminResponse resp = userService.updateUserProfile(userId, workspace, reqs);
        return ApiResponse.success(SuccessStatus.OK, resp);
    }

    // 관리자 권한 회원 상태 변경
    @Operation(summary = "관리자 권한 직원 상태 변경", description = """
    관리자 권한으로 유저ID와 조직을 통해 직원의 상태를 변경합니다.
    <br><br> 해당 회원의 userId 를 입력하고 알맞는 조직을 선택하세요.
    <br> 변경할 EmployeeStatus 값을 요청으로 보내세요.
    <br><br>***EmployeeStatus***
    <br>ACTIVE: 재직
    <br>LEAVE: 휴직 (비활성화)
    <br>RETIRED: 퇴직 (비활성화)
    """)

    @PatchMapping("/status/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeStatusResponse>> updateEmployeeStatus(
            Authentication authentication,
            @PathVariable Long userId,
            @RequestParam Workspace workspace,
            @RequestBody EmployeeStatusRequest reqs
    ) {
        // 로깅, 감사용
        Long adminId = Long.valueOf(authentication.getName());

        log.info("관리자ID: {} 관리자가 -> 직원ID: {} 직원의 정보를 수정했습니다. ", adminId, userId);
        EmployeeStatusResponse resp = userService.updateEmployeeStatus(userId, workspace, reqs);
        return ApiResponse.success(SuccessStatus.OK, resp);
    }
}
