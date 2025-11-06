package com.sampoom.user.api.agency.service;

import com.sampoom.user.api.agency.entity.AgencyProjection;
import com.sampoom.user.api.agency.event.AgencyEvent;
import com.sampoom.user.api.agency.repository.AgencyProjectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgencyProjectionService {

    private final AgencyProjectionRepository agencyProjectionRepository;
    private final AgencyProjectionUpdater updater;

    @Transactional
    public void apply(AgencyEvent event) {
        if (event == null || event.getPayload() == null || event.getPayload().getVendorId() == null) {
            log.warn("Invalid event: missing payload/agencyId. event={}", event);
            return;
        }

        Long agencyId = event.getPayload().getVendorId();
        Long incomingVersion = event.getVersion() == null ? 0L : event.getVersion();

        AgencyProjection existing = agencyProjectionRepository.findByAgencyId(agencyId).orElse(null);

        // 멱등성: 동일 이벤트 중복 방지
        if (existing != null &&
                existing.getLastEventId() != null &&
                existing.getLastEventId().toString().equals(event.getEventId())) {
            log.info("Duplicate event ignored: {}", event.getEventId());
            return;
        }

        // 버전 체크: 오래된 이벤트 무시
        if (existing != null &&
                incomingVersion <= (existing.getVersion() == null ? 0L : existing.getVersion())) {
            log.info("Outdated event ignored: {} (incoming: {}, current: {})",
                    event.getEventId(), incomingVersion, existing.getVersion());
            return;
        }

        // 이벤트 유형별 처리
        switch (event.getEventType()) {
            case "VendorCreated":

            case "VendorUpdated":
                updater.upsert(existing, event);
                break;
            case "VendorDeleted":
                updater.softDelete(existing, event);
                break;
            default:
                log.warn("Unknown eventType: {}", event.getEventType());
        }
    }
}
