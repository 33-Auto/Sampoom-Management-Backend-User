package com.sampoom.user.api.auth.repository;

import com.sampoom.user.api.auth.entity.AuthUserProjection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthUserProjectionRepository extends JpaRepository<AuthUserProjection, Long> {
    Optional<AuthUserProjection> findByUserId(Long userId);
}