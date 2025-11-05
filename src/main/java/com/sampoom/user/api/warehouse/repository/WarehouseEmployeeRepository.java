package com.sampoom.user.api.warehouse.repository;

import com.sampoom.user.api.factory.entity.FactoryEmployee;
import com.sampoom.user.api.warehouse.entity.WarehouseEmployee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface WarehouseEmployeeRepository extends JpaRepository<WarehouseEmployee, Long> {
    boolean existsByUserId(Long userId);
    Optional<WarehouseEmployee> findByUserId(Long userId);

    List<WarehouseEmployee> findAllByUserIdIn(Collection<Long> userIds);
    Page<WarehouseEmployee> findAllByWarehouseId(Long warehouseId, Pageable pageable);
    Page<WarehouseEmployee> findAll(Pageable pageable);
}