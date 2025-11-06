package com.sampoom.user.api.warehouse.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sampoom.user.api.warehouse.service.WarehouseProjectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseEventHandler {
    private final ObjectMapper objectMapper;
    private final WarehouseProjectionService warehouseProjectionService;

    @KafkaListener(topics = "branch-events", groupId = "branch-events-user")
    public void consume(String message) {
        try {
            WarehouseEventDto event = objectMapper.readValue(message, WarehouseEventDto.class);
            warehouseProjectionService.apply(event);
        } catch (Exception e) {
            log.error("Failed to process Kafka message: {}", message, e);
        }
    }
}
