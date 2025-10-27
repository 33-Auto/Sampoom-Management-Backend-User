package com.sampoom.user.api.user.service;

import com.sampoom.user.api.user.internal.dto.UserProfile;
import com.sampoom.user.common.exception.ConflictException;
import com.sampoom.user.common.exception.NotFoundException;
import com.sampoom.user.common.response.ErrorStatus;
import com.sampoom.user.api.user.dto.request.UserUpdateRequest;
import com.sampoom.user.api.user.dto.response.UserUpdateResponse;
import com.sampoom.user.api.user.entity.User;
import com.sampoom.user.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public void createProfile(UserProfile req) {
        // userId로 이미 생성된 회원 여부 확인
        if (userRepository.findById(req.getUserId()).isPresent()) {
            throw new ConflictException(ErrorStatus.USER_ID_DUPLICATED);
        }

        User user = User.builder()
                .id(req.getUserId())
                .userName(req.getUserName())
                .workspace(req.getWorkspace())
                .branch(req.getBranch())
                .position(req.getPosition())
                .build();

        userRepository.save(user);
    }


    @Transactional(readOnly = true)
    public UserProfile getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_BY_ID_NOT_FOUND));

        return UserProfile.builder()
                .userId(user.getId())
                .userName(user.getUserName())
                .workspace(user.getWorkspace())
                .branch(user.getBranch())
                .position(user.getPosition())
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
}