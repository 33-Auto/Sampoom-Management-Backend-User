package com.sampoom.user.api.auth.service;

import com.sampoom.user.api.auth.entity.AuthUserProjection;
import com.sampoom.user.api.auth.event.AuthUserEvent;
import com.sampoom.user.api.auth.event.AuthWarmupEvent;
import com.sampoom.user.api.auth.repository.AuthUserProjectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

        AuthUserProjection projection = repo.findByUserIdIncludingDeleted(userId).orElse(null);

        // 멱등 처리: 같은 이벤트 두 번 들어오면 무시
        if (projection != null && e.getEventId() != null && projection.getLastEventId() != null) {
            if (projection.getLastEventId().equals(e.getEventId())) return;
        }

        // 역순 이벤트(버전 낮음) 차단
        if (projection != null && incomingVer < nvl(projection.getVersion(), 0L)) return;

        switch (e.getEventType()) {
            case "AuthUserSignedUp":
            case "AuthUserUpdated":
                upsert(projection, e, incomingVer);
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
                .lastEventId(e.getEventId())
                .sourceUpdatedAt(parseOffset(String.valueOf(p.getUpdatedAt())))
                .version(ver)
                .build()
                : existing.toBuilder()
                .email(p.getEmail())
                .role(p.getRole())
                .lastEventId(e.getEventId())
                .sourceUpdatedAt(parseOffset(String.valueOf(p.getUpdatedAt())))
                .version(ver)
                .build();
        repo.save(next);
    }

    @Transactional
    public void rebuildFromWarmup(AuthWarmupEvent event) {
        log.info("[AuthUserProjectionService] Warmup 재구성 시작");

        repo.deleteAllInBatch();

        for (AuthWarmupEvent.AuthUserPayload p : event.getPayload()) {
            AuthUserProjection projection = AuthUserProjection.builder()
                    .userId(p.getUserId())
                    .email(p.getEmail())
                    .role(p.getRole())
                    .lastEventId(event.getEventId())
                    .sourceUpdatedAt(parseOffset(String.valueOf(p.getUpdatedAt())))
                    .version(p.getVersion())
                    .build();
            repo.save(projection);
        }
        log.info("[AuthUserProjectionService] Warmup 완료 ({}건)", repo.count());
    }


    private OffsetDateTime parseOffset(String iso) {
        if (iso == null || iso.isBlank()) return null;
        try {
            // 오프셋(+09:00 등)이 포함된 경우
            return OffsetDateTime.parse(iso);
        } catch (Exception ex) {
            // 오프셋이 없는 경우(LocalDateTime)
            return LocalDateTime.parse(iso).atOffset(ZoneOffset.ofHours(9)); // 기본 오프셋 지정
        }
    }

    private long nvl(Long v, long d) { return v == null ? d : v; }
}
