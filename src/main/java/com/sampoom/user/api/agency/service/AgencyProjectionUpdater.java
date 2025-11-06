package com.sampoom.user.api.agency.service;

import com.sampoom.user.api.agency.entity.AgencyProjection;
import com.sampoom.user.api.agency.entity.VendorStatus;
import com.sampoom.user.api.agency.event.AgencyEvent;
import com.sampoom.user.api.agency.repository.AgencyProjectionRepository;
import com.sampoom.user.common.exception.InternalServerErrorException;
import com.sampoom.user.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AgencyProjectionUpdater {

    private final AgencyProjectionRepository agencyProjectionRepository;

    public void upsert(AgencyProjection existing, AgencyEvent event) {
        validateEvent(event);
        var p = event.getPayload();
        var now = OffsetDateTime.now();

        AgencyProjection next = (existing == null)
                ? AgencyProjection.builder()
                .agencyId(p.getAgencyId())
                .agencyCode(p.getAgencyCode())
                .agencyName(p.getAgencyName())
                .address(p.getAddress())
                .latitude(p.getLatitude())
                .longitude(p.getLongitude())
                .businessNumber(p.getBusinessNumber())
                .ceoName(p.getCeoName())
                .status(p.getStatus())
                .deleted(p.getDeleted())
                .version(event.getVersion())
                .lastEventId(UUID.fromString(event.getEventId()))
                .sourceUpdatedAt(OffsetDateTime.parse(event.getOccurredAt()))
                .build()
                : existing.toBuilder()
                .agencyId(p.getAgencyId())
                .agencyCode(p.getAgencyCode())
                .agencyName(p.getAgencyName())
                .address(p.getAddress())
                .latitude(p.getLatitude())
                .longitude(p.getLongitude())
                .businessNumber(p.getBusinessNumber())
                .ceoName(p.getCeoName())
                .status(p.getStatus())
                .deleted(p.getDeleted())
                .version(event.getVersion())
                .lastEventId(UUID.fromString(event.getEventId()))
                .deleted(p.getDeleted())
                .sourceUpdatedAt(OffsetDateTime.parse(event.getOccurredAt()))
                .build();

        agencyProjectionRepository.save(next);
    }

    public void softDelete(AgencyProjection existing, AgencyEvent event) {
        if (existing == null) return;
        validateEvent(event);
        var now = OffsetDateTime.now();
        AgencyProjection next = existing.toBuilder()
                .version(event.getVersion())
                .lastEventId(UUID.fromString(event.getEventId()))
                .deleted(true)
                .status(VendorStatus.INACTIVE)
                .sourceUpdatedAt(OffsetDateTime.parse(event.getOccurredAt()))
                .updatedAt(now)
                .build();
        agencyProjectionRepository.save(next);
    }

    private void validateEvent(AgencyEvent event) {
        try {
            UUID.fromString(event.getEventId());
            OffsetDateTime.parse(event.getOccurredAt());
        } catch (IllegalArgumentException | DateTimeParseException e) {
            throw new IllegalArgumentException(ErrorStatus.INVALID_EVENT_FORMAT.getMessage(), e);
        }
        if (event.getPayload() == null || event.getPayload().getAgencyId() == null) {
            throw new IllegalArgumentException(ErrorStatus.INVALID_EVENT_FORMAT.getMessage());
        }
    }
}