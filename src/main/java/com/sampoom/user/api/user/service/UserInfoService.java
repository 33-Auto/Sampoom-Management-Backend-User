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
import com.sampoom.user.api.user.dto.response.UserInfoListResponse;
import com.sampoom.user.api.user.dto.response.UserInfoResponse;
import com.sampoom.user.api.user.entity.User;
import com.sampoom.user.api.user.repository.UserRepository;
import com.sampoom.user.api.warehouse.entity.WarehouseEmployee;
import com.sampoom.user.api.warehouse.entity.WarehouseProjection;
import com.sampoom.user.api.warehouse.repository.WarehouseEmployeeRepository;
import com.sampoom.user.api.warehouse.repository.WarehouseProjectionRepository;
import com.sampoom.user.common.entity.Workspace;
import com.sampoom.user.common.exception.BadRequestException;
import com.sampoom.user.common.response.ErrorStatus;
import io.micrometer.common.lang.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserInfoService {

    private final UserRepository userRepository;
    private final AuthUserProjectionRepository authUserProjectionRepository;
    private final FactoryEmployeeRepository factoryEmployeeRepository;
    private final FactoryProjectionRepository factoryProjectionRepository;
    private final WarehouseEmployeeRepository warehouseEmployeeRepository;
    private final WarehouseProjectionRepository warehouseProjectionRepository;
    private final AgencyEmployeeRepository agencyEmployeeRepository;
    private final AgencyProjectionRepository agencyProjectionRepository;

    public UserInfoListResponse getUsersInfo(
            Pageable pageable,
            @Nullable Workspace workspace,
            @Nullable Long organizationId
    ) {
        if (workspace == null && organizationId != null) {
            throw new BadRequestException(ErrorStatus.INVALID_REQUEST_ORGID);
        }
        // userIds: userId만 모아둔 Set
        Set<Long> userIds = new HashSet<>();
        Page<User> userPage;

        // workspace가 null → 전체 사용자 조회
        if (workspace == null) {
            userPage = userRepository.findAll(pageable);
            userIds.addAll(userPage.getContent().stream().map(User::getId).toList());
            if (userIds.isEmpty()) {
                return UserInfoListResponse.of(Page.empty(pageable));
            }
            return buildUserInfoListResponse(userPage, userPage, pageable, userIds);
        }

        // workspace별로 분기 (factory / warehouse / agency)
        // list: Employee들을 모아둔 List
        switch (workspace) {
            case FACTORY -> {
                Page<FactoryEmployee> factoryPage = (organizationId == null)
                        ? factoryEmployeeRepository.findAll(pageable)
                        : factoryEmployeeRepository.findAllByFactoryId(organizationId, pageable);
                userIds.addAll(factoryPage.getContent().stream()
                        .map(FactoryEmployee::getUserId)
                        .toList());
                if (userIds.isEmpty()) {
                    return UserInfoListResponse.of(Page.empty(pageable));
                }
                Page<User> users = userRepository.findAllByIdIn(userIds, pageable);
                return buildUserInfoListResponse(users, factoryPage, pageable, userIds);
            }
            case WAREHOUSE -> {
                Page<WarehouseEmployee> warehousePage = (organizationId == null)
                        ? warehouseEmployeeRepository.findAll(pageable)
                        : warehouseEmployeeRepository.findAllByWarehouseId(organizationId, pageable);
                userIds.addAll(warehousePage.getContent().stream()
                        .map(WarehouseEmployee::getUserId)
                        .toList());
                if (userIds.isEmpty()) {
                    return UserInfoListResponse.of(Page.empty(pageable));
                }
                Page<User> users = userRepository.findAllByIdIn(userIds, pageable);
                return buildUserInfoListResponse(users, warehousePage, pageable, userIds);
            }
            case AGENCY -> {
                Page<AgencyEmployee> agencyPage = (organizationId == null)
                        ? agencyEmployeeRepository.findAll(pageable)
                        : agencyEmployeeRepository.findAllByAgencyId(organizationId, pageable);
                userIds.addAll(agencyPage.getContent().stream()
                        .map(AgencyEmployee::getUserId)
                        .toList());
                if (userIds.isEmpty()) {
                    return UserInfoListResponse.of(Page.empty(pageable));
                }
                Page<User> users = userRepository.findAllByIdIn(userIds, pageable);
                return buildUserInfoListResponse(users, agencyPage, pageable, userIds);
            }
            default -> throw new BadRequestException(ErrorStatus.INVALID_WORKSPACE_TYPE);
        }
    }

    // 조직 별 검색 응답 빌더
    private UserInfoListResponse buildUserInfoListResponse(
            Page<User> users,
            Page<?> employeePage,
            Pageable pageable,
            Collection<Long> userIds
    ) {
        // AuthUserProjection
        Map<Long, AuthUserProjection> authMap = authUserProjectionRepository.findAllByUserIdIn(userIds)
                .stream()
                .collect(Collectors.toMap(AuthUserProjection::getUserId, a -> a, (existing, r) -> existing));

        // Employee 맵
        Map<Long, FactoryEmployee> factoryMap = factoryEmployeeRepository.findAllByUserIdIn(userIds)
                .stream().collect(Collectors.toMap(FactoryEmployee::getUserId, f -> f, (e, r) -> e));
        Map<Long, WarehouseEmployee> warehouseMap = warehouseEmployeeRepository.findAllByUserIdIn(userIds)
                .stream().collect(Collectors.toMap(WarehouseEmployee::getUserId, w -> w, (e, r) -> e));
        Map<Long, AgencyEmployee> agencyMap = agencyEmployeeRepository.findAllByUserIdIn(userIds)
                .stream().collect(Collectors.toMap(AgencyEmployee::getUserId, a -> a, (e, r) -> e));

        // projection 매핑
        Map<Long, String> factoryNameMap = factoryProjectionRepository
                .findAllByFactoryIdIn(factoryMap.values().stream()
                        .map(FactoryEmployee::getFactoryId)
                        .collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(FactoryProjection::getFactoryId, FactoryProjection::getName));

        Map<Long, String> warehouseNameMap = warehouseProjectionRepository
                .findAllByWarehouseIdIn(warehouseMap.values().stream()
                        .map(WarehouseEmployee::getWarehouseId)
                        .collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(WarehouseProjection::getWarehouseId, WarehouseProjection::getName));

        Map<Long, String> agencyNameMap = agencyProjectionRepository
                .findAllByAgencyIdIn(agencyMap.values().stream()
                        .map(AgencyEmployee::getAgencyId)
                        .collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(AgencyProjection::getAgencyId, AgencyProjection::getName));

        // DTO 변환
        List<UserInfoResponse> userInfoList = users.stream()
                .map(u -> {
                    AuthUserProjection auth = authMap.get(u.getId());
                    FactoryEmployee f = factoryMap.get(u.getId());
                    WarehouseEmployee w = warehouseMap.get(u.getId());
                    AgencyEmployee a = agencyMap.get(u.getId());

                    UserInfoResponse.UserInfoResponseBuilder builder = UserInfoResponse.builder()
                            .userId(u.getId())
                            .userName(u.getUserName())
                            .email(auth != null ? auth.getEmail() : null)
                            .role(auth != null ? auth.getRole() : null);

                    if (f != null) {
                        builder.workspace(Workspace.FACTORY)
                                .organizationId(f.getFactoryId())
                                .branch(factoryNameMap.get(f.getFactoryId()))
                                .position(f.getPosition())
                                .startedAt(f.getStartedAt())
                                .endedAt(f.getEndedAt());
                    } else if (w != null) {
                        builder.workspace(Workspace.WAREHOUSE)
                                .organizationId(w.getWarehouseId())
                                .branch(warehouseNameMap.get(w.getWarehouseId()))
                                .position(w.getPosition())
                                .startedAt(w.getStartedAt())
                                .endedAt(w.getEndedAt());
                    } else if (a != null) {
                        builder.workspace(Workspace.AGENCY)
                                .organizationId(a.getAgencyId())
                                .branch(agencyNameMap.get(a.getAgencyId()))
                                .position(a.getPosition())
                                .startedAt(a.getStartedAt())
                                .endedAt(a.getEndedAt());
                    } else {
                        builder.workspace(null);
                    }

                    return builder.build();
                })
                .toList();

        return UserInfoListResponse.of(new PageImpl<>(userInfoList, pageable, employeePage.getTotalElements()));
    }
}