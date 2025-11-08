package com.sampoom.user.api.user.service;

import com.sampoom.user.api.agency.entity.AgencyEmployee;
import com.sampoom.user.api.agency.entity.AgencyProjection;
import com.sampoom.user.api.agency.repository.AgencyEmployeeRepository;
import com.sampoom.user.api.agency.repository.AgencyProjectionRepository;
import com.sampoom.user.api.auth.entity.AuthUserProjection;
import com.sampoom.user.api.auth.repository.AuthUserProjectionRepository;
import com.sampoom.user.api.factory.entity.FactoryEmployee;
import com.sampoom.user.api.factory.entity.FactoryProjection;
import com.sampoom.user.api.factory.repository.FactoryEmployeeRepository;
import com.sampoom.user.api.factory.repository.FactoryProjectionRepository;
import com.sampoom.user.api.user.dto.request.EmployeeStatusRequest;
import com.sampoom.user.api.user.dto.request.UserUpdateAdminRequest;
import com.sampoom.user.api.user.dto.response.EmployeeStatusResponse;
import com.sampoom.user.api.user.dto.response.UserLoginResponse;
import com.sampoom.user.api.user.dto.response.UserUpdateAdminResponse;
import com.sampoom.user.api.user.internal.dto.LoginRequest;
import com.sampoom.user.api.user.internal.dto.LoginResponse;
import com.sampoom.user.api.user.internal.dto.SignupUser;
import com.sampoom.user.api.warehouse.entity.WarehouseEmployee;
import com.sampoom.user.api.warehouse.entity.WarehouseProjection;
import com.sampoom.user.api.warehouse.repository.WarehouseEmployeeRepository;
import com.sampoom.user.api.warehouse.repository.WarehouseProjectionRepository;
import com.sampoom.user.common.entity.EmployeeStatus;
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

    private final UserRepository userRepo;
    private final AuthUserProjectionRepository authUserRepo;
    private final FactoryProjectionRepository factoryRepo;
    private final FactoryEmployeeRepository factoryEmpRepo;
    private final WarehouseProjectionRepository warehouseRepo;
    private final WarehouseEmployeeRepository warehouseEmpRepo;
    private final AgencyProjectionRepository agencyRepo;
    private final AgencyEmployeeRepository agencyEmpRepo;

    // Auth Feign
    @Transactional
    public void createProfile(SignupUser req) {
        // userId로 이미 생성된 회원 여부 확인
        if (userRepo.findById(req.getUserId()).isPresent()) {
            throw new ConflictException(ErrorStatus.DUPLICATED_USER_ID);
        }

        // User:
        User user = User.builder()
                .id(req.getUserId())
                .userName(req.getUserName())
                .build();
        userRepo.save(user);

        // Employee:
        if (req.getWorkspace() == null) {
            throw new BadRequestException(ErrorStatus.INVALID_WORKSPACE_TYPE);
        }

        Workspace workspace;
        try {
            workspace = req.getWorkspace();
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(ErrorStatus.INVALID_WORKSPACE_TYPE);
        }

        switch (workspace) {
            case FACTORY -> {
                FactoryProjection factory = factoryRepo.findByName(req.getBranch())
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_FACTORY_NAME));

                FactoryEmployee factoryEmp = FactoryEmployee.builder()
                        .factoryId(factory.getFactoryId())
                        .build();

                factoryEmp.setUserId(req.getUserId());
                factoryEmp.updatePosition(req.getPosition());
                factoryEmpRepo.save(factoryEmp);
            }

            case WAREHOUSE -> {
                WarehouseProjection warehouse = warehouseRepo.findByName(req.getBranch())
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_WAREHOUSE_NAME));

                WarehouseEmployee warehouseEmp = WarehouseEmployee.builder()
                        .warehouseId(warehouse.getWarehouseId())
                        .build();
                warehouseEmp.setUserId(req.getUserId());
                warehouseEmp.updatePosition(req.getPosition());
                warehouseEmpRepo.save(warehouseEmp);
            }

            case AGENCY -> {
                AgencyProjection agency = agencyRepo.findByName(req.getBranch())
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_AGENCY_NAME));

                AgencyEmployee agencyEmp = AgencyEmployee.builder()
                        .agencyId(agency.getAgencyId())
                        .build();
                agencyEmp.setUserId(req.getUserId());
                agencyEmp.updatePosition(req.getPosition());
                agencyEmpRepo.save(agencyEmp);
            }

            default -> throw new BadRequestException(ErrorStatus.INVALID_WORKSPACE_TYPE);
        }
    }

    @Transactional
    public LoginResponse verifyWorkspace(LoginRequest req) {
        if (req.getWorkspace() == null) {
            throw new BadRequestException(ErrorStatus.INVALID_WORKSPACE_TYPE);
        }
            boolean valid = switch (req.getWorkspace()) {
                case FACTORY -> factoryEmpRepo.existsByUserId(req.getUserId());
                case WAREHOUSE -> warehouseEmpRepo.existsByUserId(req.getUserId());
                case AGENCY -> agencyEmpRepo.existsByUserId(req.getUserId());
            };

            if (!valid) {
                throw new NotFoundException(ErrorStatus.NOT_FOUND_USER_BY_WORKSPACE);
            }

            return LoginResponse.builder()
                .userId(req.getUserId())
                .workspace(req.getWorkspace())
                .valid(true)
                .build();
    }


    @Transactional(readOnly = true)
    public UserLoginResponse getMyProfile(Long userId, Workspace workspace) {
        if (userId == null || userId <= 0)
            throw new BadRequestException(ErrorStatus.INVALID_INPUT_VALUE);
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_USER_BY_ID));
        AuthUserProjection authUser = authUserRepo.findByUserId(userId)
                .orElseThrow(()-> new NotFoundException(ErrorStatus.NOT_FOUND_USER_BY_ID));

        switch (workspace) {
            case FACTORY -> {
                FactoryEmployee emp = factoryEmpRepo.findByUserId(userId)
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_FACTORY_EMPLOYEE));

                // projection 테이블에서 branch 이름 조회
                String branchName = factoryRepo.findByFactoryId(emp.getFactoryId())
                        .map(FactoryProjection::getName)
                        .orElse(null);

                return UserLoginResponse.builder()
                        .userId(userId)
                        .email(authUser.getEmail())
                        .role(authUser.getRole())
                        .userName(user.getUserName())
                        .workspace(workspace)
                        .organizationId(emp.getFactoryId())
                        .branch(branchName)
                        .position(emp.getPosition())
                        .startedAt(emp.getStartedAt())
                        .endedAt(emp.getEndedAt())
                        .build();
            }
            case WAREHOUSE -> {
                WarehouseEmployee emp = warehouseEmpRepo.findByUserId(userId)
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_WAREHOUSE_EMPLOYEE));

                String branchName = warehouseRepo.findByWarehouseId(emp.getWarehouseId())
                        .map(WarehouseProjection::getName)
                        .orElse(null);

                return UserLoginResponse.builder()
                        .userId(userId)
                        .email(authUser.getEmail())
                        .role(authUser.getRole())
                        .userName(user.getUserName())
                        .workspace(workspace)
                        .organizationId(emp.getWarehouseId())
                        .branch(branchName)
                        .position(emp.getPosition())
                        .startedAt(emp.getStartedAt())
                        .endedAt(emp.getEndedAt())
                        .build();
            }
            case AGENCY -> {
                AgencyEmployee emp = agencyEmpRepo.findByUserId(userId)
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_AGENCY_EMPLOYEE));

                String branchName = agencyRepo.findByAgencyId(emp.getAgencyId())
                        .map(AgencyProjection::getName)
                        .orElse(null);

                return UserLoginResponse.builder()
                        .userId(userId)
                        .email(authUser.getEmail())
                        .role(authUser.getRole())
                        .userName(user.getUserName())
                        .workspace(workspace)
                        .organizationId(emp.getAgencyId())
                        .branch(branchName)
                        .position(emp.getPosition())
                        .startedAt(emp.getStartedAt())
                        .endedAt(emp.getEndedAt())
                        .build();
            }

            default -> throw new BadRequestException(ErrorStatus.INVALID_WORKSPACE_TYPE);
        }
    }

    @Transactional
    public UserUpdateResponse updateMyProfile(Long userId, UserUpdateRequest req) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_USER_BY_ID));

        // null 아닌 필드만 수정 (Dirty Checking 사용)
        if (req.getUserName() != null) {
            user.setUserName(req.getUserName());
        }

        // 반환 DTO 생성
        return UserUpdateResponse.builder()
                .userId(user.getId())
                .userName(user.getUserName())
                .build();
    }

    @Transactional
    public UserUpdateAdminResponse updateUserProfile(Long userId, Workspace workspace, UserUpdateAdminRequest req) {
        if (userId == null || workspace == null || req == null || req.getPosition() == null) {
            throw new BadRequestException(ErrorStatus.INVALID_INPUT_VALUE);
        }
        Position newPosition = req.getPosition();
        User user = userRepo.findById(userId)
                .orElseThrow(()->new NotFoundException(ErrorStatus.NOT_FOUND_USER_BY_ID));

        switch (workspace) {
            case FACTORY -> {
                FactoryEmployee emp = factoryEmpRepo.findByUserId(userId)
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_FACTORY_EMPLOYEE));
                emp.updatePosition(newPosition);
                return UserUpdateAdminResponse.builder()
                        .userId(emp.getUserId())
                        .userName(user.getUserName())
                        .workspace(workspace)
                        .position(emp.getPosition())
                        .build();
            }
            case WAREHOUSE -> {
                WarehouseEmployee emp = warehouseEmpRepo.findByUserId(userId)
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_WAREHOUSE_EMPLOYEE));
                emp.updatePosition(newPosition);
                return UserUpdateAdminResponse.builder()
                        .userId(emp.getUserId())
                        .userName(user.getUserName())
                        .workspace(workspace)
                        .position(emp.getPosition())
                        .build();
            }
            case AGENCY -> {
                AgencyEmployee emp = agencyEmpRepo.findByUserId(userId)
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_AGENCY_EMPLOYEE));
                emp.updatePosition(newPosition);
                return UserUpdateAdminResponse.builder()
                        .userId(emp.getUserId())
                        .userName(user.getUserName())
                        .workspace(workspace)
                        .position(emp.getPosition())
                        .build();
            }
            default -> throw new BadRequestException(ErrorStatus.INVALID_WORKSPACE_TYPE);
        }
    }

    @Transactional
    public EmployeeStatusResponse updateEmployeeStatus(Long userId, Workspace workspace, EmployeeStatusRequest req) {
        if (userId == null || workspace == null || req == null || req.getEmployeeStatus() == null) {
            throw new BadRequestException(ErrorStatus.INVALID_INPUT_VALUE);
        }
        EmployeeStatus newEmployeeStatus = req.getEmployeeStatus();
        User user = userRepo.findById(userId)
                .orElseThrow(()->new NotFoundException(ErrorStatus.NOT_FOUND_USER_BY_ID));

        switch (workspace) {
            case FACTORY -> {
                FactoryEmployee emp = factoryEmpRepo.findByUserId(userId)
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_FACTORY_EMPLOYEE));
                emp.updateEmployeeStatus(newEmployeeStatus);
                return EmployeeStatusResponse.builder()
                        .userId(emp.getUserId())
                        .userName(user.getUserName())
                        .workspace(workspace)
                        .employeeStatus(emp.getStatus())
                        .build();
            }
            case WAREHOUSE -> {
                WarehouseEmployee emp = warehouseEmpRepo.findByUserId(userId)
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_WAREHOUSE_EMPLOYEE));
                emp.updateEmployeeStatus(newEmployeeStatus);
                return EmployeeStatusResponse.builder()
                        .userId(emp.getUserId())
                        .userName(user.getUserName())
                        .workspace(workspace)
                        .employeeStatus(emp.getStatus())
                        .build();
            }
            case AGENCY -> {
                AgencyEmployee emp = agencyEmpRepo.findByUserId(userId)
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_AGENCY_EMPLOYEE));
                emp.updateEmployeeStatus(newEmployeeStatus);
                return EmployeeStatusResponse.builder()
                        .userId(emp.getUserId())
                        .userName(user.getUserName())
                        .workspace(workspace)
                        .employeeStatus(emp.getStatus())
                        .build();
            }
            default -> throw new BadRequestException(ErrorStatus.INVALID_WORKSPACE_TYPE);
        }
    }
}