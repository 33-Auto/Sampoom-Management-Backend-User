package com.sampoom.user.api.factory.repository;

import com.sampoom.user.api.factory.entity.FactoryProjection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface FactoryProjectionRepository extends JpaRepository<FactoryProjection, Long> {

    Optional<FactoryProjection> findByFactoryId(Long factoryId);


}
