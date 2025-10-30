package com.sampoom.user.api.warehouse.repository;

import com.sampoom.user.api.warehouse.entity.WarehouseProjection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WarehouseProjectionRepository extends JpaRepository<WarehouseProjection, Long> {
    Optional<WarehouseProjection> findByWarehouseId(Long warehouseId);
    Optional<WarehouseProjection> findByName(String name);
}
