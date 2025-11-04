package com.sampoom.user.api.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sampoom.user.api.auth.entity.AuthUserProjection;
import com.sampoom.user.api.auth.repository.AuthUserProjectionRepository;
import com.sampoom.user.common.entity.Role;
import com.sampoom.user.common.exception.InternalServerErrorException;
import com.sampoom.user.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthUserProjectionService {
    private final ObjectMapper objectMapper;
    private final AuthUserProjectionRepository authUserProjectionRepository;

    @Transactional
    public void applyAuthEvent(String message) throws JsonProcessingException {
        try {
            var root = objectMapper.readTree(message);
            if (root == null || !root.has("payload") || !root.has("eventId")) {
                throw new InternalServerErrorException(ErrorStatus.INVALID_EVENT_FORMAT);
            }
            var payload = root.get("payload");
            if (payload == null || !payload.has("userId")) {
                    throw new InternalServerErrorException(ErrorStatus.INVALID_EVENT_FORMAT);
            }

            // 이벤트 메타데이터
            String eventId = getTextSafely(root, "eventId", "unknown");
            Long version = root.has("version") && !root.get("version").isNull()
                    ? root.get("version").asLong()
                    : 0L;

            var userId = payload.get("userId").asLong();
            var email = getTextSafely(payload, "email", null);
            var role = parseRole(getTextSafely(payload, "role", null));

            var existingOpt = authUserProjectionRepository.findByUserId(userId);

            AuthUserProjection projection;
            if (existingOpt.isPresent()) {
                var existing = existingOpt.get();
                if (existing.getLastEventId() != null && existing.getLastEventId().equals(eventId)) {
                    return;
                }
                if (version <= existing.getVersion()) {
                    return;
                }
                projection = existing.toBuilder()
                        .email(email)
                        .role(role)
                        .version(version)
                        .lastEventId(eventId)
                        .build();
            } else {
                projection = AuthUserProjection.builder()
                        .userId(userId)
                        .email(email)
                        .role(role)
                        .version(version)
                        .lastEventId(eventId)
                        .build();
            }
            authUserProjectionRepository.save(projection);
        }  catch (JsonProcessingException | IllegalArgumentException ex) {
            throw new InternalServerErrorException(ErrorStatus.INVALID_EVENT_FORMAT);
        }
        catch (Exception e) {
            throw new InternalServerErrorException(ErrorStatus.EVENT_PROCESSING_FAILED);
        }
    }
    private String getTextSafely(JsonNode node, String field, String defaultValue) {
        return (node == null || !node.has(field) || node.get(field).isNull())
                ? defaultValue
                : node.get(field).asText();
    }

    private Role parseRole(String roleStr) {
        if (roleStr == null || roleStr.isBlank()) return Role.USER;
        try {
            return Role.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown role value: {}, defaulting to USER", roleStr);
            return Role.USER;
        }
    }
}
