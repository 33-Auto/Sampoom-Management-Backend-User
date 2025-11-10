package com.sampoom.user.api.user.service;

import com.sampoom.user.api.agency.entity.AgencyEmployee;
import com.sampoom.user.api.agency.entity.AgencyProjection;
import com.sampoom.user.api.agency.repository.AgencyEmployeeRepository;
import com.sampoom.user.api.agency.repository.AgencyProjectionRepository;
import com.sampoom.user.api.auth.entity.AuthUserProjection;
import com.sampoom.user.api.auth.repository.AuthUserProjectionRepository;
import com.sampoom.user.api.member.entity.ProductionMember;
import com.sampoom.user.api.factory.entity.FactoryProjection;
import com.sampoom.user.api.member.repository.ProductionMemberRepository;
import com.sampoom.user.api.factory.repository.FactoryProjectionRepository;
import com.sampoom.user.api.user.dto.response.UserInfoListResponse;
import com.sampoom.user.api.user.dto.response.UserInfoResponse;
import com.sampoom.user.api.user.entity.User;
import com.sampoom.user.api.user.repository.UserRepository;
import com.sampoom.user.api.warehouse.entity.WarehouseProjection;
import com.sampoom.user.api.warehouse.repository.WarehouseProjectionRepository;
import com.sampoom.user.common.entity.Role;
import com.sampoom.user.common.entity.Workspace;
import com.sampoom.user.common.exception.BadRequestException;
import com.sampoom.user.common.response.ErrorStatus;
import jakarta.annotation.Nullable;
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
    private final ProductionMemberRepository productionMemberRepository;
    private final FactoryProjectionRepository factoryProjectionRepository;
    private final InventoryMemberRepository inventoryMemberRepository;
    private final WarehouseProjectionRepository warehouseProjectionRepository;
    private final AgencyEmployeeRepository agencyEmployeeRepository;
    private final AgencyProjectionRepository agencyProjectionRepository;

    public UserInfoListResponse getUsersInfo(
            Pageable pageable,
            @Nullable Role role,
            @Nullable Long organizationId
    ) {
        if (role == null && organizationId != null) {
            throw new BadRequestException(ErrorStatus.INVALID_REQUEST_ORGID);
        }
        // userIds: userId만 모아둔 Set
        Set<Long> userIds = new HashSet<>();
        Page<User> userPage;

        // role이 null → 전체 사용자 조회
        if (role == null) {
            userPage = userRepository.findAll(pageable);
            userIds.addAll(userPage.getContent().stream().map(User::getId).toList());
            if (userIds.isEmpty()) {
                return UserInfoListResponse.of(Page.empty(pageable));
            }
            return buildUserInfoListResponse(userPage.getContent(), userPage, pageable, userIds);
        }

        // role별로 분기 (factory / warehouse / agency)
        // list: Employee들을 모아둔 List
        switch (role) {
            case PRODUCTION -> {
                Page<ProductionMember> factoryPage = (organizationId == null)
                        ? productionMemberRepository.findAll(pageable)
                        : productionMemberRepository.findAllByFactoryId(organizationId, pageable);
                userIds.addAll(factoryPage.getContent().stream()
                        .map(ProductionMember::getUserId)
                        .toList());
                if (userIds.isEmpty()) {
                    return UserInfoListResponse.of(Page.empty(pageable));
                }
                List<User> users = userRepository.findAllByIdIn(userIds);
                return buildUserInfoListResponse(users, factoryPage, pageable, userIds);
            }
            case INVENTORY -> {
                Page<InventoryMember> warehousePage = (organizationId == null)
                        ? inventoryMemberRepository.findAll(pageable)
                        : inventoryMemberRepository.findAllByWarehouseId(organizationId, pageable);
                userIds.addAll(warehousePage.getContent().stream()
                        .map(InventoryMember::getUserId)
                        .toList());
                if (userIds.isEmpty()) {
                    return UserInfoListResponse.of(Page.empty(pageable));
                }
                List<User> users = userRepository.findAllByIdIn(userIds);
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
                List<User> users = userRepository.findAllByIdIn(userIds);
                return buildUserInfoListResponse(users, agencyPage, pageable, userIds);
            }
            default -> throw new BadRequestException(ErrorStatus.INVALID_WORKSPACE_TYPE);
        }
    }

    // 조직 별 검색 응답 빌더
    private UserInfoListResponse buildUserInfoListResponse(
            List<User> users,
            Page<?> employeePage,
            Pageable pageable,
            Collection<Long> userIds
    ) {
        // AuthUserProjection
        Map<Long, AuthUserProjection> authMap = authUserProjectionRepository.findAllByUserIdIn(userIds)
                .stream()
                .collect(Collectors.toMap(AuthUserProjection::getUserId, a -> a, (existing, r) -> existing));

        // Employee 맵
        Map<Long, ProductionMember> factoryMap = productionMemberRepository.findAllByUserIdIn(userIds)
                .stream().collect(Collectors.toMap(ProductionMember::getUserId, f -> f, (e, r) -> e));
        Map<Long, InventoryMember> warehouseMap = inventoryMemberRepository.findAllByUserIdIn(userIds)
                .stream().collect(Collectors.toMap(InventoryMember::getUserId, w -> w, (e, r) -> e));
        Map<Long, AgencyEmployee> agencyMap = agencyEmployeeRepository.findAllByUserIdIn(userIds)
                .stream().collect(Collectors.toMap(AgencyEmployee::getUserId, a -> a, (e, r) -> e));

        // projection 매핑
        Map<Long, String> factoryNameMap = factoryProjectionRepository
                .findAllByFactoryIdIn(factoryMap.values().stream()
                        .map(ProductionMember::getFactoryId)
                        .collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(FactoryProjection::getFactoryId, FactoryProjection::getName));

        Map<Long, String> warehouseNameMap = warehouseProjectionRepository
                .findAllByWarehouseIdIn(warehouseMap.values().stream()
                        .map(InventoryMember::getWarehouseId)
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
                    ProductionMember f = factoryMap.get(u.getId());
                    InventoryMember w = warehouseMap.get(u.getId());
                    AgencyEmployee a = agencyMap.get(u.getId());

                    UserInfoResponse.UserInfoResponseBuilder builder = UserInfoResponse.builder()
                            .userId(u.getId())
                            .userName(u.getUserName())
                            .email(auth != null ? auth.getEmail() : null)
                            .role(auth != null ? auth.getRole() : null);

                    if (f != null) {
                        builder.workspace(Workspace.PRODUCTION)
                                .organizationId(f.getFactoryId())
                                .branch(factoryNameMap.get(f.getFactoryId()))
                                .position(f.getPosition())
                                .status(f.getStatus())
                                .createdAt(f.getCreatedAt())
                                .startedAt(f.getStartedAt())
                                .endedAt(f.getEndedAt())
                                .deletedAt(f.getDeletedAt());
                    } else if (w != null) {
                        builder.workspace(Workspace.INVENTORY)
                                .organizationId(w.getWarehouseId())
                                .branch(warehouseNameMap.get(w.getWarehouseId()))
                                .position(w.getPosition())
                                .status(w.getStatus())
                                .createdAt(w.getCreatedAt())
                                .startedAt(w.getStartedAt())
                                .endedAt(w.getEndedAt())
                                .deletedAt(w.getDeletedAt());
                    } else if (a != null) {
                        builder.workspace(Workspace.AGENCY)
                                .organizationId(a.getAgencyId())
                                .branch(agencyNameMap.get(a.getAgencyId()))
                                .position(a.getPosition())
                                .status(a.getStatus())
                                .createdAt(a.getCreatedAt())
                                .startedAt(a.getStartedAt())
                                .endedAt(a.getEndedAt())
                                .deletedAt(a.getDeletedAt());
                    } else {
                        builder.workspace(null);
                    }

                    return builder.build();
                })
                .toList();

        return UserInfoListResponse.of(new PageImpl<>(userInfoList, pageable, employeePage.getTotalElements()));
    }
}