package com.sampoom.user.api.warehouse.repository;

import com.sampoom.user.api.factory.entity.FactoryProjection;
import com.sampoom.user.api.warehouse.entity.WarehouseProjection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface WarehouseProjectionRepository extends JpaRepository<WarehouseProjection, Long> {
    Optional<WarehouseProjection> findByWarehouseId(Long warehouseId);
    Optional<WarehouseProjection> findByName(String name);
    List<WarehouseProjection> findAllByWarehouseIdIn(Set<Long> warehouseIds);
}
