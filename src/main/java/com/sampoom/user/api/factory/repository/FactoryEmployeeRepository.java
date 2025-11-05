package com.sampoom.user.api.factory.repository;

import com.sampoom.user.api.factory.entity.FactoryEmployee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FactoryEmployeeRepository extends JpaRepository<FactoryEmployee, Long> {
    boolean existsByUserId(Long userId);
    Optional<FactoryEmployee> findByUserId(Long userId);

    List<FactoryEmployee> findAllByUserIdIn(Collection<Long> userIds);
    Page<FactoryEmployee> findAllByFactoryId(Long factoryId, Pageable pageable);
}
