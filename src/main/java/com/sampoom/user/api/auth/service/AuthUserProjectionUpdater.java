package com.sampoom.user.api.auth.service;

import com.sampoom.user.api.auth.entity.AuthUserProjection;
import com.sampoom.user.api.auth.event.AuthUserEvent;
import com.sampoom.user.api.auth.repository.AuthUserProjectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthUserProjectionUpdater {

    private final AuthUserProjectionRepository repo;

    /** 공통 검증 */
    public void validateEvent(AuthUserEvent e) {
        if (e.getPayload() == null || e.getPayload().getUserId() == null) {
            throw new IllegalArgumentException("Invalid AuthUserEvent: missing userId or payload");
        }
    }

    /** 신규 생성 또는 업데이트 (UserSignedUp, AuthUserUpdated 등) */
    public void upsert(AuthUserEvent e) {
        var p = e.getPayload();
        var existing = repo.findByUserId(p.getUserId()).orElse(null);

        var next = (existing == null)
                ? AuthUserProjection.builder()
                .userId(p.getUserId())
                .email(p.getEmail())
                .role(p.getRole())
                .deleted(Boolean.TRUE.equals(p.getDeleted()))
                .version(e.getVersion())
                .lastEventId(UUID.fromString(e.getEventId()))
                .sourceUpdatedAt(OffsetDateTime.parse(e.getOccurredAt()))
                .updatedAt(OffsetDateTime.now())
                .build()
                : existing.toBuilder()
                .email(p.getEmail() != null ? p.getEmail() : existing.getEmail())
                .role(p.getRole() != null ? p.getRole() : existing.getRole())
                .deleted(Boolean.TRUE.equals(p.getDeleted()))
                .version(e.getVersion())
                .lastEventId(UUID.fromString(e.getEventId()))
                .sourceUpdatedAt(OffsetDateTime.parse(e.getOccurredAt()))
                .updatedAt(OffsetDateTime.now())
                .build();

        repo.save(next);
        log.info("[AuthUserProjection] upsert: userId={}, email={}, role={}", p.getUserId(), p.getEmail(), p.getRole());
    }

    /** Soft Delete (UserDeactivated 등) */
    public void softDelete(AuthUserEvent e) {
        var p = e.getPayload();
        repo.findByUserId(p.getUserId()).ifPresent(proj -> {
            var next = proj.toBuilder()
                    .deleted(true)
                    .version(e.getVersion())
                    .lastEventId(UUID.fromString(e.getEventId()))
                    .sourceUpdatedAt(OffsetDateTime.parse(e.getOccurredAt()))
                    .updatedAt(OffsetDateTime.now())
                    .build();
            repo.save(next);
            log.info("[AuthUserProjection] softDelete: userId={}", p.getUserId());
        });
    }
}
