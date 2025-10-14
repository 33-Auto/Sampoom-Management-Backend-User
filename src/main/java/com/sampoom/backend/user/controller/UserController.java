package com.sampoom.backend.user.controller;


import com.sampoom.backend.user.common.response.ApiResponse;
import com.sampoom.backend.user.common.response.SuccessStatus;
import com.sampoom.backend.user.controller.dto.request.SignupRequest;
import com.sampoom.backend.user.controller.dto.request.UserUpdateRequest;
import com.sampoom.backend.user.controller.dto.response.UserUpdateResponse;
import com.sampoom.backend.user.external.dto.UserResponse;
import com.sampoom.backend.user.controller.dto.response.SignupResponse;
import com.sampoom.backend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    @PatchMapping("/update/{id}")
    public ResponseEntity<ApiResponse<UserUpdateResponse>> patchUser(
            @PathVariable Long id,
            @RequestBody UserUpdateRequest reqs
    ) {
        UserUpdateResponse resp = userService.updatePartialUser(id,reqs );
        return ApiResponse.success(SuccessStatus.OK, resp);
    }


    @GetMapping("/email/{email:.+}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@PathVariable String email) {
        UserResponse resp = userService.getUserByEmail(email);
        return ApiResponse.success(SuccessStatus.OK, resp);
    }
}
