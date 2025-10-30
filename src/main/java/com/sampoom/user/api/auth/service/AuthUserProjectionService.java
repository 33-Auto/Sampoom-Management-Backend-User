package com.sampoom.user.api.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sampoom.user.api.auth.entity.AuthUserProjection;
import com.sampoom.user.api.auth.repository.AuthUserProjectionRepository;
import com.sampoom.user.common.entity.Role;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthUserProjectionService {
    private final ObjectMapper objectMapper;
    private final AuthUserProjectionRepository authUserProjectionRepository;

    @Transactional
    public void applyAuthEvent(String message) throws JsonProcessingException {
        try {
            var root = objectMapper.readTree(message);
            var payload = root.get("payload");

            // 이벤트에서 eventId, version 추출
            String eventId = root.get("eventId").asText();
            Long version = root.has("version") ? root.get("version").asLong() : 0L;

            var projection = authUserProjectionRepository.findByUserId(payload.get("userId").asLong())
                    .map(existing -> {
                        // 중복 이벤트 차단
                        if (existing.getLastEventId() != null && existing.getLastEventId().equals(eventId)) {
                            return null; // 중복
                        }
                        // 역순 이벤트 차단
                        if (version <= existing.getVersion()) {
                            return null; // 오래된 이벤트
                        }
                        return existing.toBuilder()
                                .email(payload.get("email").asText())
                                .role(Role.valueOf(payload.get("role").asText()))
                                .version(version)
                                .lastEventId(eventId)
                                .build();
                    })
                    .orElseGet(() -> AuthUserProjection.builder()
                            .userId(payload.get("userId").asLong())
                            .email(payload.get("email").asText())
                            .role(Role.valueOf(payload.get("role").asText()))
                            .version(version)
                            .lastEventId(eventId)
                            .build());
                if (projection != null) {
                authUserProjectionRepository.save(projection);
            }
        } catch (Exception e) {
            // 메시지 내용과 에러를 함께 로깅
            System.err.println("[AuthUserProjectionService] Failed to process event: " + message);
            e.printStackTrace();
            // rollback 유도
            throw new RuntimeException("Failed to apply auth event", e);
        }
    }
}
