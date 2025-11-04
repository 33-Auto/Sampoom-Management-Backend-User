package com.sampoom.user.api.factory.repository;

import com.sampoom.user.api.factory.entity.FactoryProjection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;


public interface FactoryProjectionRepository extends JpaRepository<FactoryProjection, Long> {
    Optional<FactoryProjection> findByFactoryId(Long factoryId);
    Optional<FactoryProjection> findByName(String name);
    List<FactoryProjection> findAllByFactoryIdIn(Set<Long> factoryIds);
}
