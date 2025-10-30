package com.sampoom.user.api.user.service;

import com.sampoom.user.api.agency.entity.AgencyEmployee;
import com.sampoom.user.api.agency.repository.AgencyEmployeeRepository;
import com.sampoom.user.api.auth.entity.AuthUserProjection;
import com.sampoom.user.api.auth.repository.AuthUserProjectionRepository;
import com.sampoom.user.api.factory.entity.FactoryEmployee;
import com.sampoom.user.api.factory.repository.FactoryEmployeeRepository;
import com.sampoom.user.api.user.dto.response.UserInfoListResponse;
import com.sampoom.user.api.user.dto.response.UserInfoResponse;
import com.sampoom.user.api.user.entity.User;
import com.sampoom.user.api.user.repository.UserRepository;
import com.sampoom.user.api.warehouse.entity.WarehouseEmployee;
import com.sampoom.user.api.warehouse.repository.WarehouseEmployeeRepository;
import com.sampoom.user.common.entity.Position;
import com.sampoom.user.common.entity.Workspace;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserInfoService {
    private final UserRepository userRepository;
    private final AuthUserProjectionRepository authUserProjectionRepository;
    private final FactoryEmployeeRepository factoryEmployeeRepository;
    private final WarehouseEmployeeRepository warehouseEmployeeRepository;
    private final AgencyEmployeeRepository agencyEmployeeRepository;

    public UserInfoListResponse getAllUsers(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);

        List<UserInfoResponse> users = userPage.stream()
                .map(u -> {
                    AuthUserProjection auth = authUserProjectionRepository.findByUserId(u.getId()).orElse(null);

                    // 기본 정보
                    UserInfoResponse.UserInfoResponseBuilder builder = UserInfoResponse.builder()
                            .userId(u.getId())
                            .email(auth != null ? auth.getEmail() : null)
                            .role(auth != null ? auth.getRole() : null)
                            .userName(u.getUserName());

                    // workspace별 직원 정보 분기
                    FactoryEmployee factoryEmp = factoryEmployeeRepository.findByUserId(u.getId()).orElse(null);
                    WarehouseEmployee warehouseEmp = warehouseEmployeeRepository.findByUserId(u.getId()).orElse(null);
                    AgencyEmployee agencyEmp = agencyEmployeeRepository.findByUserId(u.getId()).orElse(null);

                    if (factoryEmp != null) {
                        builder.workspace(Workspace.FACTORY)
                                .branch(String.valueOf(factoryEmp.getFactoryId()))
                                .position(factoryEmp.getPosition())
                                .startedAt(factoryEmp.getStartedAt())
                                .endedAt(factoryEmp.getEndedAt());
                    } else if (warehouseEmp != null) {
                        builder.workspace(Workspace.WAREHOUSE)
                                .branch(String.valueOf(warehouseEmp.getWarehouseId()))
                                .position(warehouseEmp.getPosition())
                                .startedAt(warehouseEmp.getStartedAt())
                                .endedAt(warehouseEmp.getEndedAt());
                    } else if (agencyEmp != null) {
                        builder.workspace(Workspace.AGENCY)
                                .branch(String.valueOf(agencyEmp.getAgencyId()))
                                .position(agencyEmp.getPosition())
                                .startedAt(agencyEmp.getStartedAt())
                                .endedAt(agencyEmp.getEndedAt());
                    } else {
                        builder.workspace(null);
                    }

                    return builder.build();
                })
                .toList();

        return UserInfoListResponse.of(new PageImpl<>(users, pageable, userPage.getTotalElements()));
    }
}