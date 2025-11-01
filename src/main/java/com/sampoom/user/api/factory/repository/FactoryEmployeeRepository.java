package com.sampoom.user.api.factory.repository;

import com.sampoom.user.api.factory.entity.FactoryEmployee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FactoryEmployeeRepository extends JpaRepository<FactoryEmployee, Long> {
    Optional<FactoryEmployee> findByUserId(Long userId);
    List<FactoryEmployee> findAllByUserIdIn(List<Long> userIds);
}
