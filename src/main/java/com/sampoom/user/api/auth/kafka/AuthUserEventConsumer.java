package com.sampoom.user.api.auth.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sampoom.user.api.auth.event.AuthUserEvent;
import com.sampoom.user.api.auth.service.AuthUserProjectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthUserEventConsumer {

    private final ObjectMapper objectMapper;
    private final AuthUserProjectionService authUserProjectionService;

    @KafkaListener(topics = "${app.topics.auth-events:auth-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void handle(String message, Acknowledgment ack) {
        try {
            AuthUserEvent e = objectMapper.readValue(message, AuthUserEvent.class);
            authUserProjectionService.apply(e);
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("AuthUserEvent handling failed: {}", ex.toString(), ex);
            throw new RuntimeException(ex);
        }
    }
}
