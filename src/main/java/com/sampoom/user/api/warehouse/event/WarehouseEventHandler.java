package com.sampoom.user.api.warehouse.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sampoom.user.api.warehouse.service.WarehouseProjectionService;
import com.sampoom.user.common.exception.InternalServerErrorException;
import com.sampoom.user.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseEventHandler {
    private final ObjectMapper objectMapper;
    private final WarehouseProjectionService warehouseProjectionService;

    @KafkaListener(topics = "branch-events", groupId = "branch-events-users")
    public void consume(String message, Acknowledgment ack) {
        try {
            WarehouseEventDto event = objectMapper.readValue(message, WarehouseEventDto.class);
            warehouseProjectionService.apply(event);
        } catch (JsonProcessingException e) {
            log.error("Invalid Kafka event format: {}", message, e);
            throw new RuntimeException(ErrorStatus.INVALID_EVENT_FORMAT.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error processing Kafka message: {}", message, e);
            ack.acknowledge();
            throw new RuntimeException(ErrorStatus.INTERNAL_SERVER_ERROR.getMessage(), e);
        }
    }
}
