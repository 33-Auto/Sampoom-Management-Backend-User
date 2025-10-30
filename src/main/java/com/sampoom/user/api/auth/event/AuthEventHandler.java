package com.sampoom.user.api.auth.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sampoom.user.api.auth.repository.AuthUserProjectionRepository;
import com.sampoom.user.api.auth.entity.AuthUserProjection;
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
    private final AuthUserProjectionRepository authUserProjectionRepository;

    @KafkaListener(topics = "${app.topics.auth-events:auth-events}",groupId = "auth-events-user")
    @Transactional
    public void authEventHandle(String message, Acknowledgment ack) {
        try {
            var root = objectMapper.readTree(message);
            var payload = root.get("payload");

            var projection = authUserProjectionRepository.findByUserId(payload.get("userId").asLong())
                    .map(existing -> existing.toBuilder()
                            .email(payload.get("email").asText())
                            .role(Role.valueOf(payload.get("role").asText()))
                            .build())
                    .orElseGet(() -> AuthUserProjection.builder()
                            .userId(payload.get("userId").asLong())
                            .email(payload.get("email").asText())
                            .role(Role.valueOf(payload.get("role").asText()))
                            .build());

            authUserProjectionRepository.save(projection);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[AuthUserEventHandler] failed", e);
            throw new RuntimeException(e);
        }
    }
}