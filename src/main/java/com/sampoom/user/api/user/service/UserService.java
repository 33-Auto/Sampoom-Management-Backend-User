package com.sampoom.user.api.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sampoom.user.api.agency.entity.AgencyEmployee;
import com.sampoom.user.api.agency.entity.AgencyProjection;
import com.sampoom.user.api.agency.repository.AgencyEmployeeRepository;
import com.sampoom.user.api.agency.repository.AgencyProjectionRepository;
import com.sampoom.user.api.auth.entity.AuthUserProjection;
import com.sampoom.user.api.auth.repository.AuthUserProjectionRepository;
import com.sampoom.user.api.member.entity.*;
import com.sampoom.user.api.member.repository.*;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.UUID;

import static com.sampoom.user.common.entity.Workspace.AGENCY;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    // 회원 정보
    private final UserRepository userRepo;
    private final AuthUserProjectionRepository authUserRepo;

    private final AgencyEmployeeRepository agencyEmpRepo;
    private final ProductionMemberRepository prodRepo;
    private final InventoryMemberRepository invenRepo;
    private final PurchaseMemberRepository purchaseRepo;
    private final SalesMemberRepository salesRepo;
    private final MDMemberRepository mdRepo;
    private final HRMemberRepository hrRepo;


    // 기준 정보(지점용)
    private final AgencyProjectionRepository agencyRepo;
    private static final String ADMIN_BRANCH = "본사";

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
        Workspace workspace;
        try {
            workspace = req.getWorkspace();
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(ErrorStatus.INVALID_ROLE_TYPE);
        }

        // 부서 신규 회원 등록
        BaseMemberEntity emp;
        if (workspace == AGENCY) {
            AgencyProjection agency = agencyRepo.findByName(req.getBranch())
                    .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_AGENCY_NAME));

            AgencyEmployee e = AgencyEmployee.builder()
                    .agencyId(agency.getAgencyId())
                    .build();
            e.setUserId(req.getUserId());
            e.updatePosition(req.getPosition());
            agencyEmpRepo.save(e);
            emp = e;
        } else {
            // 저장한 직원 조회 ( getStatus를 반환하기 위해 )
            switch (workspace) {
                case PRODUCTION -> {
                    ProductionMember m = prodRepo.findByUserId(user.getId())
                            .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER_PRODUCTION));
                    m.setUserId(req.getUserId());
                    m.setPosition(req.getPosition());
                    prodRepo.save(m);
                    emp = m;
                }

                case INVENTORY -> {
                    InventoryMember m = invenRepo.findByUserId(user.getId())
                            .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER_INVENTORY));
                    m.setUserId(req.getUserId());
                    m.setPosition(req.getPosition());
                    invenRepo.save(m);
                    emp = m;
                }
                case PURCHASE -> {
                    PurchaseMember m = purchaseRepo.findByUserId(user.getId())
                            .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER_PURCHASE));
                    m.setUserId(req.getUserId());
                    m.setPosition(req.getPosition());
                    purchaseRepo.save(m);
                    emp = m;
                }
                case SALES -> {
                    SalesMember m = salesRepo.findByUserId(user.getId())
                            .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER_SALES));
                    m.setUserId(req.getUserId());
                    m.setPosition(req.getPosition());
                    salesRepo.save(m);
                    emp = m;
                }
                case MD -> {
                    MDMember m = mdRepo.findByUserId(user.getId())
                            .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER_MD));
                    m.setUserId(req.getUserId());
                    m.setPosition(req.getPosition());
                    mdRepo.save(m);
                    emp = m;
                }
                case HR -> {
                    HRMember m = hrRepo.findByUserId(user.getId())
                            .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER_HR));
                    m.setUserId(req.getUserId());
                    m.setPosition(req.getPosition());
                    hrRepo.save(m);
                    emp = m;
                }
                default -> throw new BadRequestException(ErrorStatus.INVALID_ROLE_TYPE);
            }
        }

        entityManager.flush();  // version 필드 초기화를 위해 flush

        // 이벤트 발행
        UserCreatedEvent evt = UserCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("UserCreated")
                .occurredAt(OffsetDateTime.now().toString())
                .version(emp.getVersion())
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
    public UserLoginResponse getMyProfile(Long userId, Collection<? extends GrantedAuthority> authorities) {
        Workspace workspace = null;
        for (GrantedAuthority authority : authorities) {
            String name = authority.getAuthority().replace("ROLE_", "");
            try {
                workspace = Workspace.valueOf(name);
                break; // workspace 찾으면 바로 종료
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (workspace == null)
            throw new BadRequestException(ErrorStatus.INVALID_TOKEN);

        if (userId == null || userId <= 0)
            throw new BadRequestException(ErrorStatus.INVALID_INPUT_VALUE);

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_USER_BY_ID));
        AuthUserProjection authUser = authUserRepo.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_USER_BY_ID));

        BaseMemberEntity emp;
        String branchName;
        Long orgId = null;

        switch (workspace) {
            case AGENCY -> {
                var e = agencyEmpRepo.findByUserId(userId)
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_EMPLOYEE_AGENCY));

                emp = e;
                orgId = e.getAgencyId();
                branchName = agencyRepo.findByAgencyId(e.getAgencyId())
                        .map(AgencyProjection::getName)
                        .orElse(null);
            }

            case PRODUCTION -> {
                emp = prodRepo.findByUserId(userId)
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER_PRODUCTION));
                branchName = "생산 관리";
            }

            case INVENTORY -> {
                emp = invenRepo.findByUserId(userId)
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER_INVENTORY));
                branchName = "재고 관리";
            }

            case PURCHASE -> {
                emp = purchaseRepo.findByUserId(userId)
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER_PURCHASE));
                branchName = "구매 관리";
            }

            case SALES -> {
                emp = salesRepo.findByUserId(userId)
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER_SALES));
                branchName = "판매 관리";
            }

            case MD -> {
                emp = mdRepo.findByUserId(userId)
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER_MD));
                branchName = "기준 정보 관리";
            }

            case HR -> {
                emp = hrRepo.findByUserId(userId)
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER_HR));
                branchName = "인사 관리";
            }

            default -> throw new BadRequestException(ErrorStatus.INVALID_ROLE_TYPE);
        }

        return UserLoginResponse.builder()
                .userId(userId)
                .email(authUser.getEmail())
                .workspace(workspace)
                .userName(user.getUserName())
                .organizationId(orgId)
                .branch(branchName)
                .position(emp.getPosition())
                .startedAt(emp.getStartedAt())
                .endedAt(emp.getEndedAt())
                .build();
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

        BaseMemberEntity emp;
        switch (workspace) {
            case AGENCY -> {
                emp = agencyEmpRepo.findByUserId(userId)
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_EMPLOYEE_AGENCY));
                emp.updatePosition(newPosition);
            }

            case PRODUCTION -> {
                emp = prodRepo.findByUserId(userId)
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER_PRODUCTION));
                emp.updatePosition(newPosition);
            }

            case INVENTORY -> {
                emp = invenRepo.findByUserId(userId)
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER_INVENTORY));
                emp.updatePosition(newPosition);
            }

            case PURCHASE -> {
                emp = purchaseRepo.findByUserId(userId)
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER_PURCHASE));
                emp.updatePosition(newPosition);
            }

            case SALES -> {
                emp = salesRepo.findByUserId(userId)
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER_SALES));
                emp.updatePosition(newPosition);
            }

            case MD -> {
                emp = mdRepo.findByUserId(userId)
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER_MD));
                emp.updatePosition(newPosition);
            }

            case HR -> {
                emp = hrRepo.findByUserId(userId)
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER_HR));
                emp.updatePosition(newPosition);
            }
            default -> throw new BadRequestException(ErrorStatus.INVALID_ROLE_TYPE);
        }
        return UserUpdateAdminResponse.builder()
                .userId(emp.getUserId())
                .userName(user.getUserName())
                .position(emp.getPosition())
                .build();
    }

    @Transactional
    public EmployeeStatusResponse updateEmployeeStatus(Long userId, Workspace workspace, EmployeeStatusRequest req) {
        if (userId == null || workspace == null || req == null || req.getEmployeeStatus() == null) {
            throw new BadRequestException(ErrorStatus.INVALID_INPUT_VALUE);
        }
        EmployeeStatus newEmployeeStatus = req.getEmployeeStatus();
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_USER_BY_ID));

        BaseMemberEntity emp = switch (workspace) {
            case AGENCY -> agencyEmpRepo.findByUserId(userId)
                    .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_EMPLOYEE_AGENCY));
            case PRODUCTION -> prodRepo.findByUserId(userId)
                    .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER_PRODUCTION));
            case INVENTORY -> invenRepo.findByUserId(userId)
                    .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER_INVENTORY));
            case PURCHASE -> purchaseRepo.findByUserId(userId)
                    .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER_PURCHASE));
            case SALES -> salesRepo.findByUserId(userId)
                    .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER_SALES));
            case MD -> mdRepo.findByUserId(userId)
                    .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER_MD));
            case HR -> hrRepo.findByUserId(userId)
                    .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER_HR));
            default -> throw new BadRequestException(ErrorStatus.INVALID_ROLE_TYPE);
        };
        // 똑같으면 변화없음
        if (emp.getStatus() == newEmployeeStatus) {
            return EmployeeStatusResponse.builder()
                    .userId(emp.getUserId())
                    .userName(user.getUserName())
                    .employeeStatus(emp.getStatus())
                    .build();
        }
        // 수정 (변화없음도 기록되게)
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
                .employeeStatus(emp.getStatus())
                .build();
    }
}