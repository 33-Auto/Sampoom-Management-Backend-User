package com.sampoom.user.api.user.service;

import com.sampoom.user.api.agency.entity.AgencyEmployee;
import com.sampoom.user.api.agency.entity.AgencyProjection;
import com.sampoom.user.api.agency.repository.AgencyEmployeeRepository;
import com.sampoom.user.api.agency.repository.AgencyProjectionRepository;
import com.sampoom.user.api.factory.entity.FactoryEmployee;
import com.sampoom.user.api.factory.entity.FactoryProjection;
import com.sampoom.user.api.factory.repository.FactoryEmployeeRepository;
import com.sampoom.user.api.factory.repository.FactoryProjectionRepository;
import com.sampoom.user.api.user.internal.dto.SignupUser;
import com.sampoom.user.api.warehouse.entity.WarehouseEmployee;
import com.sampoom.user.api.warehouse.entity.WarehouseProjection;
import com.sampoom.user.api.warehouse.repository.WarehouseEmployeeRepository;
import com.sampoom.user.api.warehouse.repository.WarehouseProjectionRepository;
import com.sampoom.user.common.entity.Position;
import com.sampoom.user.common.entity.Workspace;
import com.sampoom.user.common.exception.BadRequestException;
import com.sampoom.user.common.exception.ConflictException;
import com.sampoom.user.common.exception.NotFoundException;
import com.sampoom.user.common.response.ErrorStatus;
import com.sampoom.user.api.user.dto.request.UserUpdateRequest;
import com.sampoom.user.api.user.dto.response.UserUpdateResponse;
import com.sampoom.user.api.user.entity.User;
import com.sampoom.user.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FactoryProjectionRepository factoryProjectionRepository;
    private final FactoryEmployeeRepository factoryEmployeeRepository;
    private final WarehouseProjectionRepository warehouseProjectionRepository;
    private final WarehouseEmployeeRepository warehouseEmployeeRepository;
    private final AgencyProjectionRepository agencyProjectionRepository;
    private final AgencyEmployeeRepository agencyEmployeeRepository;


    @Transactional
    public void createProfile(SignupUser req) {
        // userId로 이미 생성된 회원 여부 확인
        if (userRepository.findById(req.getUserId()).isPresent()) {
            throw new ConflictException(ErrorStatus.USER_ID_DUPLICATED);
        }

        // User:
        User user = User.builder()
                .id(req.getUserId())
                .userName(req.getUserName())
                .build();
        userRepository.save(user);

        // Employee:
        if (req.getWorkspace() == null) {
            throw new BadRequestException(ErrorStatus.INVALID_WORKSPACE_TYPE);
        }
        Workspace workspace = Workspace.valueOf(req.getWorkspace().toUpperCase());
        switch (workspace) {
            case FACTORY -> {
                FactoryProjection factory = factoryProjectionRepository.findByName(req.getBranch())
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.FACTORY_NAME_NOT_FOUND));

                factoryEmployeeRepository.save(FactoryEmployee.builder()
                        .position(req.getPosition())
                        .userId(req.getUserId())
                        .factoryId(factory.getFactoryId())
                        .build());
            }

            case WAREHOUSE -> {
                WarehouseProjection warehouse = warehouseProjectionRepository.findByName(req.getBranch())
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.WAREHOUSE_NAME_NOT_FOUND));

                warehouseEmployeeRepository.save(WarehouseEmployee.builder()
                        .position(req.getPosition())
                        .userId(req.getUserId())
                        .warehouseId(warehouse.getWarehouseId())
                        .build());
            }

            case AGENCY -> {
                AgencyProjection agency = agencyProjectionRepository.findByName(req.getBranch())
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.AGENCY_NAME_NOT_FOUND));

                agencyEmployeeRepository.save(AgencyEmployee.builder()
                        .position(req.getPosition())
                        .userId(req.getUserId())
                        .agencyId(agency.getAgencyId())
                        .build());
            }

            default -> throw new BadRequestException(ErrorStatus.INVALID_WORKSPACE_TYPE);
        }
    }


    @Transactional(readOnly = true)
    public User getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_BY_ID_NOT_FOUND));

        return User.builder()
                .id(user.getId())
                .userName(user.getUserName())
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