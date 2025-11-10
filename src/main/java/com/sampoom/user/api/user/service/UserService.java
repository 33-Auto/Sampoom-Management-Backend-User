package com.sampoom.user.api.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sampoom.user.api.agency.entity.AgencyEmployee;
import com.sampoom.user.api.agency.entity.AgencyProjection;
import com.sampoom.user.api.agency.repository.AgencyEmployeeRepository;
import com.sampoom.user.api.agency.repository.AgencyProjectionRepository;
import com.sampoom.user.api.auth.entity.AuthUserProjection;
import com.sampoom.user.api.auth.repository.AuthUserProjectionRepository;
import com.sampoom.user.api.member.entity.*;
import com.sampoom.user.api.factory.entity.FactoryProjection;
import com.sampoom.user.api.member.repository.*;
import com.sampoom.user.api.factory.repository.FactoryProjectionRepository;
import com.sampoom.user.api.user.dto.request.EmployeeStatusRequest;
import com.sampoom.user.api.user.dto.request.UserUpdateAdminRequest;
import com.sampoom.user.api.user.dto.response.EmployeeStatusResponse;
import com.sampoom.user.api.user.dto.response.UserLoginResponse;
import com.sampoom.user.api.user.dto.response.UserUpdateAdminResponse;
import com.sampoom.user.api.user.event.EmployeeUpdatedEvent;
import com.sampoom.user.api.user.event.UserCreatedEvent;
import com.sampoom.user.api.user.internal.dto.SignupUser;
import com.sampoom.user.api.user.outbox.OutboxEvent;
import com.sampoom.user.api.user.outbox.OutboxRepository;
import com.sampoom.user.api.warehouse.entity.WarehouseProjection;
import com.sampoom.user.api.warehouse.repository.WarehouseProjectionRepository;
import com.sampoom.user.common.entity.*;
import com.sampoom.user.common.exception.BadRequestException;
import com.sampoom.user.common.exception.ConflictException;
import com.sampoom.user.common.exception.InternalServerErrorException;
import com.sampoom.user.common.exception.NotFoundException;
import com.sampoom.user.common.response.ErrorStatus;
import com.sampoom.user.api.user.dto.request.UserUpdateRequest;
import com.sampoom.user.api.user.dto.response.UserUpdateResponse;
import com.sampoom.user.api.user.entity.User;
import com.sampoom.user.api.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

import static com.sampoom.user.common.entity.Role.AGENCY;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    // 회원 정보
    private final UserRepository userRepo;
    private final AuthUserProjectionRepository authUserRepo;
    private final AgencyEmployeeRepository agencyEmpRepo;

    private final BaseMemberRepository baseMemberRepo;
    private final ProductionMemberRepository prodRepo;
    private final InventoryMemberRepository invenRepo;
    private final PurchaseMemberRepository purchaseRepo;
    private final SalesMemberRepository salesRepo;
    private final MDMemberRepository mdRepo;
    private final HRMemberRepository hrRepo;


    // 기준 정보(지점용)
    private final FactoryProjectionRepository factoryRepo;
    private final WarehouseProjectionRepository warehouseRepo;
    private final AgencyProjectionRepository agencyRepo;

    private final ObjectMapper objectMapper;
    private final OutboxRepository outboxRepo;

    @PersistenceContext
    private final EntityManager entityManager;

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
        Role role;
        try {
            role = req.getRole();
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(ErrorStatus.INVALID_WORKSPACE_TYPE);
        }

        // 부서 신규 회원 등록
        if (role==AGENCY) {
            AgencyProjection agency = agencyRepo.findByName(req.getBranch())
                    .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_AGENCY_NAME));

            AgencyEmployee agencyEmp = AgencyEmployee.builder()
                    .agencyId(agency.getAgencyId())
                    .build();
            agencyEmp.setUserId(req.getUserId());
            agencyEmp.updatePosition(req.getPosition());
            agencyEmpRepo.save(agencyEmp);
        }

        // 저장한 직원 조회 ( getStatus를 반환하기 위해 )
        switch (role) {
            case PRODUCTION -> {
                ProductionMember m = prodRepo.findByUserId(user.getId())
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_FACTORY_EMPLOYEE));
                m.setUserId(req.getUserId());
                m.setPosition(req.getPosition());
            }

            case INVENTORY -> {
                InventoryMember m = invenRepo.findByUserId(user.getId())
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_WAREHOUSE_EMPLOYEE));
                m.setUserId(req.getUserId());
                m.setPosition(req.getPosition());
            }
            case PURCHASE -> {
                PurchaseMember m = purchaseRepo.findByUserId(user.getId())
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_AGENCY_EMPLOYEE));
                m.setUserId(req.getUserId());
                m.setPosition(req.getPosition());
            }
            case SALES -> {
                SalesMember m = salesRepo.findByUserId(user.getId())
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_AGENCY_EMPLOYEE));
                m.setUserId(req.getUserId());
                m.setPosition(req.getPosition());
            }
            case MD -> {
                MDMember m = mdRepo.findByUserId(user.getId())
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_AGENCY_EMPLOYEE));
                m.setUserId(req.getUserId());
                m.setPosition(req.getPosition());
            }
            case HR -> {
                HRMember m = hrRepo.findByUserId(user.getId())
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_AGENCY_EMPLOYEE));
                m.setUserId(req.getUserId());
                m.setPosition(req.getPosition());
            }
            default -> throw new BadRequestException(ErrorStatus.INVALID_WORKSPACE_TYPE);
        }

        entityManager.flush();  // version 필드 초기화를 위해 flush

        // 이벤트 발행
        UserCreatedEvent evt = UserCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("UserCreated")
                .occurredAt(OffsetDateTime.now().toString())
                .version(user.getVersion())
                .payload(UserCreatedEvent.Payload.builder()
                        .userId(user.getId())
                        .employeeStatus(emp.getStatus())    // user의 근무 상태 표시 목적 user대신 emp 호출
                        .updatedAt(user.getCreatedAt())     // 생성한 시간으로 sourceUpdatedAt 하기 위해
                        .build())
                .build();
        try {
            String payloadJson = objectMapper.writeValueAsString(evt);
            outboxRepo.save(OutboxEvent.builder()
                    .eventType(evt.getEventType())
                    .aggregateId(emp.getUserId())
                    .payload(payloadJson)
                    .published(false)
                    .build());

        } catch (Exception e) {
            throw new InternalServerErrorException(ErrorStatus.OUTBOX_SERIALIZATION_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public UserLoginResponse getMyProfile(Long userId, Role role) {
        if (userId == null || userId <= 0)
            throw new BadRequestException(ErrorStatus.INVALID_INPUT_VALUE);
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_USER_BY_ID));
        AuthUserProjection authUser = authUserRepo.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_USER_BY_ID));

        switch (role) {
            case PRODUCTION -> {
                ProductionMember emp = factoryEmpRepo.findByUserId(userId)
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
            case INVENTORY -> {
                InventoryMember emp = warehouseEmpRepo.findByUserId(userId)
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
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_USER_BY_ID));

        switch (workspace) {
            case PRODUCTION -> {
                ProductionMember emp = factoryEmpRepo.findByUserId(userId)
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_FACTORY_EMPLOYEE));
                emp.updatePosition(newPosition);
                return UserUpdateAdminResponse.builder()
                        .userId(emp.getUserId())
                        .userName(user.getUserName())
                        .workspace(workspace)
                        .position(emp.getPosition())
                        .build();
            }
            case INVENTORY -> {
                InventoryMember emp = warehouseEmpRepo.findByUserId(userId)
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
    public EmployeeStatusResponse updateEmployeeStatus(Long userId, Role role, EmployeeStatusRequest req) {
        if (userId == null || role == null || req == null || req.getEmployeeStatus() == null) {
            throw new BadRequestException(ErrorStatus.INVALID_INPUT_VALUE);
        }
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_USER_BY_ID));
        EmployeeStatus newEmployeeStatus = req.getEmployeeStatus();

        BaseMemberEntity emp = switch (role) {
            case PRODUCTION -> factoryEmpRepo.findByUserId(userId)
                    .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_FACTORY_EMPLOYEE));
            case INVENTORY -> warehouseEmpRepo.findByUserId(userId)
                    .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_WAREHOUSE_EMPLOYEE));
            case AGENCY -> agencyEmpRepo.findByUserId(userId)
                    .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_AGENCY_EMPLOYEE));
            default -> throw new BadRequestException(ErrorStatus.INVALID_WORKSPACE_TYPE);
        };
        if (emp.getStatus() == newEmployeeStatus) {
            return EmployeeStatusResponse.builder()
                    .userId(emp.getUserId())
                    .userName(user.getUserName())
                    .(workspace)
                    .employeeStatus(emp.getStatus())
                    .build();
        }
        emp.onUpdateStatus(newEmployeeStatus);

        // version/updatedAt 최신화
        entityManager.flush();

        // 이벤트 발행
        EmployeeUpdatedEvent evt = EmployeeUpdatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("EmployeeUpdated")
                .occurredAt(OffsetDateTime.now().toString())
                .version(emp.getVersion())
                .payload(EmployeeUpdatedEvent.Payload.builder()
                        .userId(userId)
                        .workspace(workspace)
                        .employeeStatus(emp.getStatus())
                        .updatedAt(emp.getUpdatedAt())
                        .build())
                .build();
        try {
            String payloadJson = objectMapper.writeValueAsString(evt);
            outboxRepo.save(OutboxEvent.builder()
                    .eventType(evt.getEventType())
                    .aggregateId(emp.getUserId())
                    .payload(payloadJson)
                    .published(false)
                    .build());

        } catch (Exception e) {
            throw new InternalServerErrorException(ErrorStatus.OUTBOX_SERIALIZATION_ERROR);
        }

        return EmployeeStatusResponse.builder()
                .userId(emp.getUserId())
                .userName(user.getUserName())
                .workspace(workspace)
                .employeeStatus(emp.getStatus())
                .build();
    }
}