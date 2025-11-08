package com.sampoom.user.api.factory.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sampoom.user.api.factory.service.FactoryProjectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FactoryEventHandler {

    private final ObjectMapper objectMapper;
    private final FactoryProjectionService factoryProjectionService;

    @KafkaListener(topics = "factory-branch-events", groupId = "factory-branch-events-users")
    public void handle(String message, Acknowledgment ack) {
        try {
            FactoryEvent evt = objectMapper.readValue(message, FactoryEvent.class);
            factoryProjectionService.apply(evt); // @Transactional 은 서비스 메서드에
        } catch (Exception ex) {
            log.error("FactoryEvent handling failed: {}", ex.toString(), ex);
            ack.acknowledge();
            // 컨테이너가 오프셋 커밋을 하지 않도록 런타임 예외로 던져 재시도/ DLQ 흐름
            throw new RuntimeException("Kafka handling failed", ex);
        }
    }
}




