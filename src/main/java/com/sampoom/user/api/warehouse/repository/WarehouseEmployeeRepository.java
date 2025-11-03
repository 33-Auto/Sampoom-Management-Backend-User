package com.sampoom.user.api.warehouse.repository;

import com.sampoom.user.api.factory.entity.FactoryEmployee;
import com.sampoom.user.api.warehouse.entity.WarehouseEmployee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WarehouseEmployeeRepository extends JpaRepository<WarehouseEmployee, Long> {
    boolean existsByUserId(Long userId);
    List<WarehouseEmployee> findAllByUserIdIn(List<Long> userIds);
    Optional<WarehouseEmployee> findByUserId(Long userId);
}