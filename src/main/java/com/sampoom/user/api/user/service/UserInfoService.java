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
import com.sampoom.user.common.entity.Position;
import com.sampoom.user.common.entity.Workspace;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public UserInfoListResponse getAllUsersInfo(Pageable pageable) {

        // 모든 유저에서 userIdList 추출
        Page<User> userPage = userRepository.findAll(pageable);
        List<Long> userIds = userPage.getContent()
                .stream()
                .map(User::getId)
                .toList();
        if (userIds.isEmpty()) {
            return UserInfoListResponse.of(Page.empty(pageable));
        }

        // userId에 해당하는 모든 유저 조회
        // 해당하는 userId가 없으면 제외, 해당되는 userId로만 매핑
        Map<Long, AuthUserProjection> authMap = authUserProjectionRepository.findAllByUserIdIn(userIds)
                .stream()
                .collect(Collectors.toMap(AuthUserProjection::getUserId, a -> a,(existing, replacement) -> existing));
                                                         // key: userId / value: user / 중복키 발생 정책: (existing(기존키 유지), replacement(신규키 교체))
        Map<Long, FactoryEmployee> factoryMap = factoryEmployeeRepository.findAllByUserIdIn(userIds)
                .stream()
                .collect(Collectors.toMap(FactoryEmployee::getUserId, f -> f, (existing, replacement) -> existing));

        Map<Long, WarehouseEmployee> warehouseMap = warehouseEmployeeRepository.findAllByUserIdIn(userIds)
                .stream()
                .collect(Collectors.toMap(WarehouseEmployee::getUserId, w -> w, (existing, replacement) -> existing));

        Map<Long, AgencyEmployee> agencyMap = agencyEmployeeRepository.findAllByUserIdIn(userIds)
                .stream()
                .collect(Collectors.toMap(AgencyEmployee::getUserId, a -> a, (existing, replacement) -> existing));

        // 모든 workspace ID 수집
        Set<Long> factoryIds = factoryMap.values().stream()
                .map(FactoryEmployee::getFactoryId)
                .collect(Collectors.toSet());
        Set<Long> warehouseIds = warehouseMap.values().stream()
                .map(WarehouseEmployee::getWarehouseId)
                .collect(Collectors.toSet());
        Set<Long> agencyIds = agencyMap.values().stream()
                .map(AgencyEmployee::getAgencyId)
                .collect(Collectors.toSet());

        // 배치 조회
        Map<Long, String> factoryNameMap = factoryProjectionRepository.findAllByFactoryIdIn(factoryIds)
                .stream()
                .collect(Collectors.toMap(FactoryProjection::getFactoryId, FactoryProjection::getName));
        Map<Long, String> warehouseNameMap = warehouseProjectionRepository.findAllByWarehouseIdIn(warehouseIds)
                .stream()
                .collect(Collectors.toMap(WarehouseProjection::getWarehouseId, WarehouseProjection::getName));
        Map<Long, String> agencyNameMap = agencyProjectionRepository.findAllByAgencyIdIn(agencyIds)
                .stream()
                .collect(Collectors.toMap(AgencyProjection::getAgencyId, AgencyProjection::getName));

        // userPage(전체 User)의 Id를 통해 userInfo를 담은 객체를 생성해 List로 묶음
        List<UserInfoResponse> userInfoList = userPage.getContent().stream()
                .map(u -> {
                    AuthUserProjection auth = authMap.get(u.getId());
                    FactoryEmployee factoryEmp = factoryMap.get(u.getId());
                    WarehouseEmployee warehouseEmp = warehouseMap.get(u.getId());
                    AgencyEmployee agencyEmp = agencyMap.get(u.getId());

                    // 바로 넣을 수 있는 User, AuthUserProjection 정보 넣기
                    UserInfoResponse.UserInfoResponseBuilder builder = UserInfoResponse.builder()
                            .userId(u.getId())
                            .userName(u.getUserName())
                            .email(auth != null ? auth.getEmail() : null)
                            .role(auth != null ? auth.getRole() : null);

                    // workspace 분기 처리, Employee 정보까지 넣기 완료 -> 이후 다음 객체를 builder로 생성 및 map
                    if (factoryEmp != null) {
                        String factoryName = factoryNameMap.get(factoryEmp.getFactoryId());
                        builder.workspace(Workspace.FACTORY)
                                .branch(factoryName)
                                .position(factoryEmp.getPosition())
                                .startedAt(factoryEmp.getStartedAt())
                                .endedAt(factoryEmp.getEndedAt());
                    } else if (warehouseEmp != null) {
                        String warehouseName = warehouseNameMap.get(warehouseEmp.getWarehouseId());
                        builder.workspace(Workspace.WAREHOUSE)
                                .branch(warehouseName)
                                .position(warehouseEmp.getPosition())
                                .startedAt(warehouseEmp.getStartedAt())
                                .endedAt(warehouseEmp.getEndedAt());
                    } else if (agencyEmp != null) {
                        String agencyName = agencyNameMap.get(agencyEmp.getAgencyId());
                        builder.workspace(Workspace.AGENCY)
                                .branch(agencyName)
                                .position(agencyEmp.getPosition())
                                .startedAt(agencyEmp.getStartedAt())
                                .endedAt(agencyEmp.getEndedAt());
                    } else {
                        builder.workspace(null);
                    }

                    return builder.build();
                })
                .toList();

        // 최종 PageImpl로 감싸서 반환
        return UserInfoListResponse.of(new PageImpl<>(userInfoList, pageable, userPage.getTotalElements()));
    }
}