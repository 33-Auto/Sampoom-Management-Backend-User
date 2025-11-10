package com.sampoom.user.api.user.service;

import com.sampoom.user.api.agency.entity.AgencyEmployee;
import com.sampoom.user.api.agency.entity.AgencyProjection;
import com.sampoom.user.api.agency.repository.AgencyEmployeeRepository;
import com.sampoom.user.api.agency.repository.AgencyProjectionRepository;
import com.sampoom.user.api.auth.entity.AuthUserProjection;
import com.sampoom.user.api.auth.repository.AuthUserProjectionRepository;
import com.sampoom.user.api.member.entity.*;
import com.sampoom.user.api.member.repository.*;
import com.sampoom.user.api.user.dto.response.UserInfoListResponse;
import com.sampoom.user.api.user.dto.response.UserInfoResponse;
import com.sampoom.user.api.user.entity.User;
import com.sampoom.user.api.user.repository.UserRepository;
import com.sampoom.user.common.entity.BaseMemberEntity;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserInfoService {

    private final UserRepository userRepo;
    private final AuthUserProjectionRepository authUserRepo;

    private final AgencyEmployeeRepository agencyEmpRepo;
    private final AgencyProjectionRepository agencyRepo;

    private final ProductionMemberRepository prodRepo;
    private final InventoryMemberRepository invenRepo;
    private final PurchaseMemberRepository purchaseRepo;
    private final SalesMemberRepository salesRepo;
    private final MDMemberRepository mdRepo;
    private final HRMemberRepository hrRepo;


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

        // role이 null → 전체 사용자 조회
        if (workspace == null) {
            userPage = userRepo.findAll(pageable);
            userIds.addAll(userPage.getContent().stream().map(User::getId).toList());
            if (userIds.isEmpty()) {
                return UserInfoListResponse.of(Page.empty(pageable));
            }
            return buildUserInfoListResponse(userPage.getContent(), userPage, pageable, userIds);
        }

        // role별로 분기 (factory / warehouse / agency)
        // list: Employee들을 모아둔 List
        switch (workspace) {
            case AGENCY -> {
                Page<AgencyEmployee> agencyPage = (organizationId == null)
                        ? agencyEmpRepo.findAll(pageable)
                        : agencyEmpRepo.findAllByAgencyId(organizationId, pageable);
                userIds.addAll(agencyPage.getContent().stream()
                        .map(AgencyEmployee::getUserId)
                        .toList());
                if (userIds.isEmpty()) {
                    return UserInfoListResponse.of(Page.empty(pageable));
                }
                List<User> users = userRepo.findAllByIdIn(userIds);
                return buildUserInfoListResponse(users, agencyPage, pageable, userIds);
            }
            case PRODUCTION -> {
                Page<ProductionMember> prodPage = prodRepo.findAll(pageable);
                userIds.addAll(prodPage.getContent().stream()
                        .map(ProductionMember::getUserId)
                        .toList());
                if (userIds.isEmpty()) {
                    return UserInfoListResponse.of(Page.empty(pageable));
                }
                List<User> users = userRepo.findAllByIdIn(userIds);
                return buildUserInfoListResponse(users, prodPage, pageable, userIds);
            }
            case INVENTORY -> {
                Page<InventoryMember> invPage = invenRepo.findAll(pageable);
                userIds.addAll(invPage.getContent().stream()
                        .map(InventoryMember::getUserId)
                        .toList());
                if (userIds.isEmpty()) {
                    return UserInfoListResponse.of(Page.empty(pageable));
                }
                List<User> users = userRepo.findAllByIdIn(userIds);
                return buildUserInfoListResponse(users, invPage, pageable, userIds);
            }

            case PURCHASE -> {
                Page<PurchaseMember> purchasePage = purchaseRepo.findAll(pageable);
                userIds.addAll(purchasePage.getContent().stream()
                        .map(PurchaseMember::getUserId)
                        .toList());
                if (userIds.isEmpty()) {
                    return UserInfoListResponse.of(Page.empty(pageable));
                }
                List<User> users = userRepo.findAllByIdIn(userIds);
                return buildUserInfoListResponse(users, purchasePage, pageable, userIds);
            }

            case SALES -> {
                Page<SalesMember> salesPage = salesRepo.findAll(pageable);
                userIds.addAll(salesPage.getContent().stream()
                        .map(SalesMember::getUserId)
                        .toList());
                if (userIds.isEmpty()) {
                    return UserInfoListResponse.of(Page.empty(pageable));
                }
                List<User> users = userRepo.findAllByIdIn(userIds);
                return buildUserInfoListResponse(users, salesPage, pageable, userIds);
            }

            case MD -> {
                Page<MDMember> mdPage = mdRepo.findAll(pageable);
                userIds.addAll(mdPage.getContent().stream()
                        .map(MDMember::getUserId)
                        .toList());
                if (userIds.isEmpty()) {
                    return UserInfoListResponse.of(Page.empty(pageable));
                }
                List<User> users = userRepo.findAllByIdIn(userIds);
                return buildUserInfoListResponse(users, mdPage, pageable, userIds);
            }

            case HR -> {
                Page<HRMember> hrPage = hrRepo.findAll(pageable);
                userIds.addAll(hrPage.getContent().stream()
                        .map(HRMember::getUserId)
                        .toList());
                if (userIds.isEmpty()) {
                    return UserInfoListResponse.of(Page.empty(pageable));
                }
                List<User> users = userRepo.findAllByIdIn(userIds);
                return buildUserInfoListResponse(users, hrPage, pageable, userIds);
            }
            default -> throw new BadRequestException(ErrorStatus.INVALID_ROLE_TYPE);
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
        Map<Long, AuthUserProjection> authMap = authUserRepo.findAllByUserIdIn(userIds)
                .stream().collect(Collectors.toMap(AuthUserProjection::getUserId, a -> a, (a, b) -> a));

        // Employee 맵
        Map<Long, AgencyEmployee> agencyMap = toMap(agencyEmpRepo.findAllByUserIdIn(userIds), AgencyEmployee::getUserId);
        Map<Long, ProductionMember> prodMap = toMap(prodRepo.findAllByUserIdIn(userIds), ProductionMember::getUserId);
        Map<Long, InventoryMember> invenMap = toMap(invenRepo.findAllByUserIdIn(userIds), InventoryMember::getUserId);
        Map<Long, PurchaseMember> purchaseMap = toMap(purchaseRepo.findAllByUserIdIn(userIds), PurchaseMember::getUserId);
        Map<Long, SalesMember> salesMap = toMap(salesRepo.findAllByUserIdIn(userIds), SalesMember::getUserId);
        Map<Long, MDMember> mdMap = toMap(mdRepo.findAllByUserIdIn(userIds), MDMember::getUserId);
        Map<Long, HRMember> hrMap = toMap(hrRepo.findAllByUserIdIn(userIds), HRMember::getUserId);

        // Agency 이름 캐싱
        Map<Long, String> agencyNameMap = agencyRepo.findAllByAgencyIdIn(
                agencyMap.values().stream().map(AgencyEmployee::getAgencyId).collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(AgencyProjection::getAgencyId, AgencyProjection::getName));

        // DTO 변환
        List<UserInfoResponse> result = users.stream().map(u -> {
            AuthUserProjection auth = authMap.get(u.getId());
            if (auth == null || auth.getWorkspace() == null)
                throw new BadRequestException(ErrorStatus.INVALID_INPUT_VALUE);

            UserInfoResponse.UserInfoResponseBuilder b = baseBuilder(u, auth);

            switch (auth.getWorkspace()) {
                case AGENCY -> {
                    AgencyEmployee e = agencyMap.get(u.getId());
                    if (e != null) fillEmployeeFields(b, e, agencyNameMap.get(e.getAgencyId()), e.getAgencyId());
                }
                case PRODUCTION -> fillEmployeeFields(b, prodMap.get(u.getId()), null, null);
                case INVENTORY -> fillEmployeeFields(b, invenMap.get(u.getId()), null, null);
                case PURCHASE -> fillEmployeeFields(b, purchaseMap.get(u.getId()), null, null);
                case SALES -> fillEmployeeFields(b, salesMap.get(u.getId()), null, null);
                case MD -> fillEmployeeFields(b, mdMap.get(u.getId()), null, null);
                case HR -> fillEmployeeFields(b, hrMap.get(u.getId()), null, null);
                default -> throw new BadRequestException(ErrorStatus.INVALID_INPUT_VALUE);
            }

            return b.build();
        }).toList();

        return UserInfoListResponse.of(new PageImpl<>(result, pageable, employeePage.getTotalElements()));
    }

    private UserInfoResponse.UserInfoResponseBuilder baseBuilder(User u, AuthUserProjection auth) {
        return UserInfoResponse.builder()
                .userId(u.getId())
                .userName(u.getUserName())
                .email(auth.getEmail())
                .workspace(auth.getWorkspace());
    }

    private <T extends BaseMemberEntity> void fillEmployeeFields(
            UserInfoResponse.UserInfoResponseBuilder b,
            T emp,
            String branch,
            Long orgId
    ) {
        if (emp == null) return;
        b.branch(branch)
                .organizationId(orgId)
                .position(emp.getPosition())
                .status(emp.getStatus())
                .createdAt(emp.getCreatedAt())
                .startedAt(emp.getStartedAt())
                .endedAt(emp.getEndedAt())
                .deletedAt(emp.getDeletedAt());
    }

    private static <T> Map<Long, T> toMap(List<T> list, Function<T, Long> keyFn) {
        return list.stream().collect(Collectors.toMap(keyFn, e -> e, (a, b) -> a));
    }


}