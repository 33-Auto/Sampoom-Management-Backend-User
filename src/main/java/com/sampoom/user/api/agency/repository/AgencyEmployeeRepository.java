package com.sampoom.user.api.agency.repository;

import com.sampoom.user.api.agency.entity.AgencyEmployee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AgencyEmployeeRepository extends JpaRepository<AgencyEmployee, Long> {
    // 특정 대리점에 속한 모든 직원 조회
    List<AgencyEmployee> findByAgencyId(Long agencyId);
    Optional<AgencyEmployee> findByUserId(Long userId);
    List<AgencyEmployee> findAllByUserIdIn(List<Long> userIds);
}
