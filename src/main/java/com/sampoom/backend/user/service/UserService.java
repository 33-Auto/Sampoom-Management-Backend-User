package com.sampoom.backend.user.service;

import com.sampoom.backend.user.common.response.ErrorStatus;
import com.sampoom.backend.user.controller.dto.request.SignupRequest;
import com.sampoom.backend.user.controller.dto.request.UserUpdateRequest;
import com.sampoom.backend.user.controller.dto.response.SignupResponse;
import com.sampoom.backend.user.controller.dto.response.UserUpdateResponse;
import com.sampoom.backend.user.domain.User;
import com.sampoom.backend.user.external.dto.UserResponse;
import com.sampoom.backend.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
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

    @Transactional
    public UserUpdateResponse updatePartialUser(Long userId, UserUpdateRequest req) {
        // Repository 사용해서 DB에서 엔티티 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // null 아닌 필드만 수정 (Dirty Checking 사용)
        if (req.getUserName() != null) {
            user.setUserName(req.getUserName());
        }
        if (req.getPosition() != null) {
            user.setPosition(req.getPosition());
        }
        if (req.getWorkspace() != null) {
            user.setWorkspace(req.getWorkspace());
        }
        if (req.getBranch() != null) {
            user.setBranch(req.getBranch());
        }

        // 반환 DTO 생성
        return UserUpdateResponse.from(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(()->new IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없습니다."));

        return UserResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .userName(user.getUserName())
                .role(user.getRole())
                .password(user.getPassword())
                .build();
    }
}