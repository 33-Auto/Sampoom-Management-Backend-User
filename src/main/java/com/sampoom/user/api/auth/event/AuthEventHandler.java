package com.sampoom.user.api.auth.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sampoom.user.api.auth.repository.AuthUserProjectionRepository;
import com.sampoom.user.api.auth.entity.AuthUserProjection;
import com.sampoom.user.api.auth.service.AuthUserProjectionService;
import com.sampoom.user.common.entity.Role;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthEventHandler {

    private final ObjectMapper objectMapper;
    private final AuthUserProjectionService authUserProjectionService;

    @KafkaListener(topics = "${app.topics.auth-events:auth-events}",groupId = "auth-events-user")
    public void authEventHandle(String message, Acknowledgment ack) {
        try {
            authUserProjectionService.applyAuthEvent(message);
            ack.acknowledge(); // 트랜잭션 밖에서 호출
        } catch (Exception e) {
            log.error("[AuthUserEventHandler] failed", e);
            throw new RuntimeException(e);
        }
    }
}