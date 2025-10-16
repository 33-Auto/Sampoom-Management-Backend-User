package com.sampoom.backend.user.service;

import com.sampoom.backend.user.common.exception.ConflictException;
import com.sampoom.backend.user.common.exception.NotFoundException;
import com.sampoom.backend.user.common.exception.UnauthorizedException;
import com.sampoom.backend.user.common.response.ErrorStatus;
import com.sampoom.backend.user.controller.dto.request.SignupRequest;
import com.sampoom.backend.user.controller.dto.request.UserUpdateRequest;
import com.sampoom.backend.user.controller.dto.request.VerifyLoginRequest;
import com.sampoom.backend.user.controller.dto.response.SignupResponse;
import com.sampoom.backend.user.controller.dto.response.UserUpdateResponse;
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
        User user = User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .workspace(req.getWorkspace())
                .branch(req.getBranch())
                .userName(req.getUserName())
                .position(req.getPosition())
                .build(); // 자동 ROLE_USER, createdAt/updatedAt

        User saved;
        try {
            saved = userRepository.save(user);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new ConflictException(ErrorStatus.USER_EMAIL_DUPLICATED);
        }

        return SignupResponse.builder()
                .userId(saved.getId())
                .userName(saved.getUserName())
                .email(saved.getEmail())
                .build();
    }

    @Transactional
    public UserUpdateResponse updatePartialUser(Long userId, UserUpdateRequest req) {
        // Repository 사용해서 DB에서 엔티티 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_BY_ID_NOT_FOUND));

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
    public UserResponse verifyLogin(VerifyLoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_BY_EMAIL_NOT_FOUND));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new UnauthorizedException(ErrorStatus.USER_PASSWORD_INVALID);
        }

        return UserResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .userName(user.getUserName())
                .role(user.getRole())
                .build();
    }

}