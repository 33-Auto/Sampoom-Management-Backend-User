package com.sampoom.user.api.warehouse.service;

import com.sampoom.user.api.warehouse.entity.WarehouseProjection;
import com.sampoom.user.api.warehouse.event.WarehouseEventDto;
import com.sampoom.user.api.warehouse.repository.WarehouseProjectionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class WarehouseProjectionService {
    private final WarehouseProjectionRepository warehouseProjectionRepository;

    @Transactional
    public void updateOrCreate(WarehouseEventDto eventDto) {
        WarehouseProjection warehouseProjection = warehouseProjectionRepository.findByWarehouseId(eventDto.getId()).orElse(null);

        if (warehouseProjection == null) {
            warehouseProjection = WarehouseProjection.builder()
                    .warehouseId(eventDto.getId())
                    .name(eventDto.getName())
                    .address(eventDto.getAddress())
                    .status(eventDto.getStatus())
                    .version(eventDto.getVersion())
                    .sourceUpdatedAt(eventDto.getSourceUpdateAt())
                    .updatedAt(OffsetDateTime.now())
                    .build();
        } else {
            warehouseProjection.setName(eventDto.getName());
            warehouseProjection.setAddress(eventDto.getAddress());
            warehouseProjection.setStatus(eventDto.getStatus());
            warehouseProjection.setVersion(eventDto.getVersion());
            warehouseProjection.setSourceUpdatedAt(eventDto.getSourceUpdateAt());
            warehouseProjection.setUpdatedAt(OffsetDateTime.now());
        }
        warehouseProjectionRepository.save(warehouseProjection);
    }
}
