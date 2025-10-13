package com.sampoom.backend.user.controller;


import com.sampoom.backend.user.common.response.ApiResponse;
import com.sampoom.backend.user.common.response.SuccessStatus;
import com.sampoom.backend.user.controller.dto.request.SignupRequest;
import com.sampoom.backend.user.external.dto.UserResponse;
import com.sampoom.backend.user.controller.dto.response.SignupResponse;
import com.sampoom.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@RequestBody SignupRequest req) {
        SignupResponse resp = userService.signup(req);
        return ApiResponse.success(SuccessStatus.CREATED, resp);
    }

    @GetMapping("/email/{email}")
    public UserResponse getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email);
    }

    @GetMapping("/id/{id}")
    public UserResponse getUserById(@PathVariable("id") Long userId) {
        return userService.getUserById(userId);
    }
}
