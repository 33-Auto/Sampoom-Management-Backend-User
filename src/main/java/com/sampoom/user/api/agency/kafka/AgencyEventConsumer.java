package com.sampoom.user.api.agency.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sampoom.user.api.agency.event.AgencyEvent;
import com.sampoom.user.api.agency.service.AgencyProjectionService;
import com.sampoom.user.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgencyEventConsumer {

    private final ObjectMapper objectMapper;
    private final AgencyProjectionService agencyProjectionService;

    @KafkaListener(topics = "vendor-events", groupId = "vendor-events-user2")
    @Transactional
    public void consume(String message) {
        try {
            // JSON → 이벤트 변환
            AgencyEvent event = objectMapper.readValue(message, AgencyEvent.class);

            agencyProjectionService.apply(event);
        } catch (Exception e) {
            log.error("Kafka 이벤트 처리 실패", e);
            throw new RuntimeException(ErrorStatus.INTERNAL_SERVER_ERROR.getMessage(), e);
        }
    }
}