package com.sampoom.user.api.factory.service;

import com.sampoom.user.api.factory.entity.FactoryEmployee;
import com.sampoom.user.api.factory.entity.FactoryProjection;
import com.sampoom.user.api.factory.entity.FactoryStatus;
import com.sampoom.user.api.factory.repository.FactoryEmployeeRepository;
import com.sampoom.user.api.factory.repository.FactoryProjectionRepository;
import com.sampoom.user.common.entity.EmployeeStatus;
import com.sampoom.user.common.entity.Position;
import com.sampoom.user.common.entity.Role;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class FactoryEmployeeService {

    private final FactoryProjectionRepository factoryProjectionRepository;
    private final FactoryEmployeeRepository factoryEmployeeRepository;
    /**
     * branch(=공장명) → FactoryProjection → factoryId 매핑 후, FactoryEmployee 멱등 배정
     * @param userId   가입 유저 ID
     * @param userName (필요 시 Name 기반 정책 쓸 수 있어서 받음)
     * @param branch   공장명 (Projection의 name과 매칭)
     * @param position 직급 문자열 (예: "STAFF", "MANAGER")
     */

    public void assignFactoryEmployee(Long userId, String userName, String branch, String position) {
        if (branch == null || branch.isBlank()) return; // 브랜치 없으면 스킵

        FactoryProjection fp = factoryProjectionRepository.findByName(branch)
                .filter(p -> Boolean.FALSE.equals(p.getDeleted()) && p.getStatus() == FactoryStatus.ACTIVE)
                .orElse(null);
        if (fp == null) return; // 유효한 공장 아님 → 스킵

        Long factoryId = fp.getFactoryId();

        // 이미 배정되어 있으면 스킵(멱등)
        if (factoryEmployeeRepository.existsByUserIdAndFactoryId(userId, factoryId)) return;

        // position 문자열 → enum 변환 (안전 파싱)
        Position pos = safePosition(position);

        FactoryEmployee saved = factoryEmployeeRepository.save(
                FactoryEmployee.builder()
                        .userId(userId)
                        .factoryId(factoryId)
                        .position(pos)
                        .status(EmployeeStatus.ACTIVE)
                        .build()
        );
    }

    private Position safePosition(String s) {
        if (s == null || s.isBlank()) return Position.STAFF;
        try { return Position.valueOf(s.toUpperCase()); }
        catch (IllegalArgumentException e) { return Position.STAFF; }
    }
}
