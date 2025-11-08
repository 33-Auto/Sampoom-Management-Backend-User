package com.sampoom.user.api.auth.service;

import com.sampoom.user.api.auth.entity.AuthUserProjection;
import com.sampoom.user.api.auth.event.AuthUserEvent;
import com.sampoom.user.api.auth.repository.AuthUserProjectionRepository;
import com.sampoom.user.common.entity.Role;
import com.sampoom.user.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthUserProjectionService {

    private final AuthUserProjectionRepository repo;

    @Transactional
    public void apply(AuthUserEvent e) {
        final Long userId = e.getPayload().getUserId();
        final Long incomingVer = nvl(e.getVersion(), 0L);

        AuthUserProjection projection = repo.findById(userId).orElse(null);

        // 멱등 처리: 같은 이벤트 두 번 들어오면 무시
        if (projection != null && e.getEventId() != null && projection.getLastEventId() != null) {
            if (projection.getLastEventId().equals(e.getEventId())) return;
        }

        // 역순 이벤트(버전 낮음) 차단
        if (projection != null && incomingVer <= nvl(projection.getVersion(), 0L)) return;

        switch (e.getEventType()) {
            case "AuthUserSignedUp":
            case "AuthUserUpdated":
                upsert(projection, e, incomingVer);
                break;
            case "AuthUserDeleted":
                softDelete(projection, e, incomingVer);
                break;
            default:
                log.warn("Unknown eventType: {}", e.getEventType());
                break;
        }
    }

    private void upsert(AuthUserProjection existing, AuthUserEvent e, Long ver) {
        AuthUserEvent.Payload p = e.getPayload();

        AuthUserProjection next = (existing == null)
                ? AuthUserProjection.builder()
                .userId(p.getUserId())
                .email(p.getEmail())
                .role(p.getRole())
                .version(ver)
                .lastEventId(e.getEventId())
                .sourceUpdatedAt(p.getCreatedAt() != null ? p.getCreatedAt().atOffset(ZoneOffset.ofHours(9)) : null)
                .deleted(false)
                .deletedAt(null)
                .build()
                : existing.toBuilder()
                .email(p.getEmail())
                .role(p.getRole())
                .version(ver)
                .lastEventId(e.getEventId())
                .sourceUpdatedAt(p.getUpdatedAt() != null ? p.getUpdatedAt().atOffset(ZoneOffset.ofHours(9)) : null)
                .deleted(false)
                .deletedAt(null)
                .build();
        repo.save(next);
    }

    private void softDelete(AuthUserProjection existing, AuthUserEvent e, Long ver) {
        if (existing == null) return;
        AuthUserEvent.Payload p = e.getPayload();

        AuthUserProjection next = existing.toBuilder()
                .version(ver)
                .lastEventId(e.getEventId())
                .deleted(true)
                .deletedAt(p.getDeletedAt() != null ? p.getDeletedAt().atOffset(ZoneOffset.UTC) : null)
                .sourceUpdatedAt(p.getUpdatedAt() != null ? p.getUpdatedAt().atOffset(ZoneOffset.UTC) : null)
                .build();

        repo.save(next);
    }

    private Role parseRole(String r) {
        if (r == null || r.isBlank()) return Role.USER;
        try {
            return Role.valueOf(r);
        } catch (IllegalArgumentException ex) {
            log.warn("Unknown role value: {}, defaulting to USER", r);
            return Role.USER;
        }
    }

    private OffsetDateTime parseOffset(String iso) {
        return iso == null ? null : OffsetDateTime.parse(iso);
    }

    private long nvl(Long v, long d) { return v == null ? d : v; }
}
