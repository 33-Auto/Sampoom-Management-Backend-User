package com.sampoom.user.api.factory.service;

import com.sampoom.user.api.factory.entity.FactoryProjection;
import com.sampoom.user.api.factory.entity.FactoryStatus;
import com.sampoom.user.api.factory.event.FactoryEvent;
import com.sampoom.user.api.factory.repository.FactoryProjectionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FactoryProjectionService {

    private final FactoryProjectionRepository repo;

    @Transactional
    public void apply(FactoryEvent e) {
        final Long factoryId   = e.getPayload().getFactoryId();
        final Long incomingVer = nvl(e.getVersion(), 0L);

        FactoryProjection fp = repo.findByFactoryId(factoryId).orElse(null);

        // 멱등(같은 이벤트) 차단
        if (fp != null && e.getEventId() != null && fp.getLastEventId() != null) {
            if (fp.getLastEventId().toString().equals(e.getEventId())) return;
        }
        // 역순(오래된 이벤트) 차단
        if (fp != null && incomingVer <= nvl(fp.getVersion(), 0L)) return;

        switch (e.getEventType()) {
            case "FactoryCreated":
            case "FactoryUpdated":
                upsert(fp, e, incomingVer);
                break;
            case "FactoryDeleted":
                softDelete(fp, e, incomingVer);
                break;
            default:
                // 필요 시 로깅
                break;
        }
    }

    private void upsert(FactoryProjection fp, FactoryEvent e, Long ver) {
        FactoryEvent.Payload p = e.getPayload();

        FactoryProjection next = (fp == null)
                ? FactoryProjection.builder()
                .factoryId(p.getFactoryId())
                .name(p.getName())
                .address(p.getAddress())
                .status(parseStatus(p.getStatus()))
                .version(ver)
                .lastEventId(parseUuid(e.getEventId()))
                .deleted(p.getDeleted())
                .sourceUpdatedAt(parseOffset(e.getOccurredAt()))
                .updatedAt(OffsetDateTime.now())
                .build()
                : fp.toBuilder()
                // fp의 id, factoryId 등은 보존됨(toBuilder가 복사)
                .name(p.getName())
                .address(p.getAddress())
                .status(parseStatus(p.getStatus()))
                .version(ver)
                .lastEventId(parseUuid(e.getEventId()))
                .deleted(p.getDeleted())
                .sourceUpdatedAt(parseOffset(e.getOccurredAt()))
                .updatedAt(OffsetDateTime.now())
                .build();

        repo.save(next);
    }

    private void softDelete(FactoryProjection fp, FactoryEvent e, Long ver) {
        FactoryEvent.Payload p = e.getPayload();
        if (fp == null) {
            // 삭제 이벤트만 먼저 온 경우: 정책에 따라 무시하거나 '삭제된 스텁' 생성
            return;
        }
        FactoryProjection next = fp.toBuilder()
                .version(ver)
                .lastEventId(parseUuid(e.getEventId()))
                .deleted(p.getDeleted())
                .status(parseStatus(p.getStatus()))
                .sourceUpdatedAt(parseOffset(e.getOccurredAt()))
                .updatedAt(OffsetDateTime.now())
                .build();

        repo.save(next);
    }

    private FactoryStatus parseStatus(String s) {
        // 안전 변환 (Null/이상값 대비)
        if (s == null) return FactoryStatus.INACTIVE;
        return FactoryStatus.valueOf(s);
    }

    private UUID parseUuid(String s) {
        return s == null ? null : UUID.fromString(s);
    }

    private OffsetDateTime parseOffset(String iso) {
        return iso == null ? null : OffsetDateTime.parse(iso);
    }

    private long nvl(Long v, long d) { return v == null ? d : v; }
}