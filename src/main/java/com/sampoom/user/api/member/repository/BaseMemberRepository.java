package com.sampoom.user.api.member.repository;

import com.sampoom.user.common.entity.BaseMemberEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BaseMemberRepository<T extends BaseMemberEntity> extends JpaRepository<T, Long> {

    boolean existsByUserId(Long userId);

    Optional<T> findByUserId(Long userId);

    List<T> findAllByUserIdIn(Collection<Long> userIds);

    Page<T> findAllByFactoryId(Long factoryId, Pageable pageable);
}
