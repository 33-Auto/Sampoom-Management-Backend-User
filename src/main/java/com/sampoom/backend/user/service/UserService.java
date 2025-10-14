package com.sampoom.backend.user.service;

import com.sampoom.backend.user.common.response.ErrorStatus;
import com.sampoom.backend.user.controller.dto.request.SignupRequest;
import com.sampoom.backend.user.controller.dto.response.SignupResponse;
import com.sampoom.backend.user.domain.User;
import com.sampoom.backend.user.external.dto.UserResponse;
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
                .branch(req.getBranch())
                .userName(req.getUserName())
                .position(req.getPosition())
                .build(); // 자동 ROLE_USER, createdAt/updatedAt

        User saved = userRepository.save(user);

        return SignupResponse.builder()
                .userId(saved.getId())
                .username(saved.getUserName())
                .email(saved.getEmail())
                .build();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(()->new IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없습니다."));

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .userName(user.getUserName())
                .role(user.getRole())
                .password(user.getPassword())
                .build();
    }
}