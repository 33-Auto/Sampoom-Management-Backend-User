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

    @KafkaListener(topics = "warehouse-events", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String message) {
        try {
            WarehouseEventDto event = objectMapper.readValue(message, WarehouseEventDto.class);
            warehouseProjectionService.updateOrCreate(event);
        } catch (Exception e) {
            log.error("Failed to process Kafka message: {}", message, e);
        }
    }

    private void handleEvent(WarehouseEventDto event) {
        // 실제 처리 로직
        log.info("Processing event logic for branch: {}", event.getName());
    }
}
