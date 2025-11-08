package com.sampoom.user.api.auth.repository;

import com.sampoom.user.api.auth.entity.AuthUserProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AuthUserProjectionRepository extends JpaRepository<AuthUserProjection, Long> {
    Optional<AuthUserProjection> findByUserId(Long userId);
    List<AuthUserProjection> findAllByUserIdIn(Collection<Long> userIds);

    @Query(value = "SELECT * FROM auth_user_projection WHERE user_id = :userId", nativeQuery = true)
    Optional<AuthUserProjection> findByUserIdIncludingDeleted(@Param("userId") Long userId);
}