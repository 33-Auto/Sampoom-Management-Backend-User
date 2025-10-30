package com.sampoom.user.api.user.service;

import com.sampoom.user.api.auth.entity.AuthUserProjection;
import com.sampoom.user.api.factory.entity.FactoryEmployee;
import com.sampoom.user.api.user.internal.dto.SignupUser;
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
    private final AuthUserProjection authUserProjection;

    @Transactional
    public void createProfile(SignupUser req) {
        // userId로 이미 생성된 회원 여부 확인
        if (userRepository.findById(req.getUserId()).isPresent()) {
            throw new ConflictException(ErrorStatus.USER_ID_DUPLICATED);
        }

        // TODO: Factory/Warehouse/Agency 분기
        // TODO: 매핑할 필드: branch->name,(factoryId)

        User user = User.builder()
                .id(req.getUserId())
                .userName(req.getUserName())
                .build();

//        userRepository.save(user);
    }


    @Transactional(readOnly = true)
    public SignupUser getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_BY_ID_NOT_FOUND));

        return SignupUser.builder()
//                .userId(user.getId())
//                .userName(user.getUserName())
//                .workspace(user.getWorkspace())
//                .branch(user.getBranch())
//                .position(user.getPosition())
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

        // 반환 DTO 생성
        return UserUpdateResponse.from(user);
    }
}