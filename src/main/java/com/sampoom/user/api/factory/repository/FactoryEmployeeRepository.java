package com.sampoom.user.api.factory.repository;

import com.sampoom.user.api.factory.entity.FactoryEmployee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FactoryEmployeeRepository extends JpaRepository<FactoryEmployee, Long> {
}
