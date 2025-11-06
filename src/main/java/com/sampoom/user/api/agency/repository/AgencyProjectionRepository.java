package com.sampoom.user.api.agency.repository;

import com.sampoom.user.api.agency.entity.AgencyProjection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AgencyProjectionRepository extends JpaRepository<AgencyProjection, Long> {
    // 대리점 ID로 Projection 조회
    Optional<AgencyProjection> findByAgencyId(Long agencyId);
    Optional<AgencyProjection> findByName(String name);
    List<AgencyProjection> findAllByAgencyIdIn(Set<Long> agencyIds);
}
