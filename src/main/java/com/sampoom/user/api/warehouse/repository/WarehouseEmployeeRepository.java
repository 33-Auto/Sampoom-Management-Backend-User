package com.sampoom.user.api.warehouse.repository;

import com.sampoom.user.api.warehouse.entity.WarehouseEmployee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseEmployeeRepository extends JpaRepository<WarehouseEmployee, Long> {
}