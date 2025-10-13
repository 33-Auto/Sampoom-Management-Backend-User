package com.sampoom.backend.user.service;

import com.sampoom.backend.user.common.response.ErrorStatus;
import com.sampoom.backend.user.controller.dto.request.SignupRequest;
import com.sampoom.backend.user.controller.dto.response.SignupResponse;
import com.sampoom.backend.user.domain.User;
import com.sampoom.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignupResponse signup(SignupRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException(ErrorStatus.ALREADY_REGISTER_EMAIL_EXCEPETION.getMessage());
        }

        User user = User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .workspace(req.getWorkspace())
                .location(req.getLocation())
                .branch(req.getBranch())
                .name(req.getName())
                .position(req.getPosition())
                .build(); // 자동 ROLE_USER, createdAt/updatedAt

        User saved = userRepository.save(user);

        return SignupResponse.builder()
                .userId(saved.getId())
                .username(saved.getName())
                .email(saved.getEmail())
                .build();
    }

    @Transactional(readOnly = true)
    public boolean verifyLogin(String email, String rawPassword) {
        System.out.println("🔥 [DEBUG] verifyLogin 호출됨 - email=" + email + ", password=" + rawPassword);
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        System.out.println("🔥 DB 비밀번호: " + user.getPassword());
        System.out.println("🔥 입력 비밀번호: " + rawPassword);
        boolean result = passwordEncoder.matches(rawPassword, user.getPassword());
        System.out.println("🔥 matches 결과: " + result);
        return result;
    }
}