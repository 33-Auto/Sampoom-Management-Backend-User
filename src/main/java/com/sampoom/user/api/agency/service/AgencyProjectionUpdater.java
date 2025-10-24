package com.sampoom.user.api.agency.service;

import com.sampoom.user.api.agency.entity.AgencyProjection;
import com.sampoom.user.api.agency.event.AgencyEvent;
import com.sampoom.user.api.agency.repository.AgencyProjectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AgencyProjectionUpdater {

    private final AgencyProjectionRepository agencyProjectionRepository;

    public void upsert(AgencyProjection existing, AgencyEvent event) {
        var p = event.getPayload();
        var now = OffsetDateTime.now();

        AgencyProjection next = (existing == null)
                ? AgencyProjection.builder()
                .agencyId(p.getAgencyId())
                .name(p.getName())
                .address(p.getAddress())
                .status(p.getStatus())
                .version(event.getVersion())
                .lastEventId(UUID.fromString(event.getEventId()))
                .deleted(p.getDeleted())
                .sourceUpdatedAt(OffsetDateTime.parse(event.getOccurredAt()))
                .updatedAt(now)
                .build()
                : existing.toBuilder()
                .name(p.getName())
                .address(p.getAddress())
                .status(p.getStatus())
                .version(event.getVersion())
                .lastEventId(UUID.fromString(event.getEventId()))
                .deleted(p.getDeleted())
                .sourceUpdatedAt(OffsetDateTime.parse(event.getOccurredAt()))
                .updatedAt(now)
                .build();

        agencyProjectionRepository.save(next);
    }

    public void softDelete(AgencyProjection existing, AgencyEvent event) {
        if (existing == null) return;
        var now = OffsetDateTime.now();
        AgencyProjection next = existing.toBuilder()
                .version(event.getVersion())
                .lastEventId(UUID.fromString(event.getEventId()))
                .deleted(true)
                .status("INACTIVE")
                .sourceUpdatedAt(OffsetDateTime.parse(event.getOccurredAt()))
                .updatedAt(now)
                .build();
        agencyProjectionRepository.save(next);
    }
}