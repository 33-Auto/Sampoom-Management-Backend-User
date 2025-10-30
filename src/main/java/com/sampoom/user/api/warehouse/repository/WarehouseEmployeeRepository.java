package com.sampoom.user.api.warehouse.repository;

import com.sampoom.user.api.factory.entity.FactoryProjection;
import com.sampoom.user.api.warehouse.entity.WarehouseEmployee;
import com.sampoom.user.api.warehouse.entity.WarehouseProjection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WarehouseEmployeeRepository extends JpaRepository<WarehouseEmployee, Long> {
}