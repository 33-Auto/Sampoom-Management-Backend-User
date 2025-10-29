package com.sampoom.user.api.user.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sampoom.user.api.agency.repository.AgencyProjectionRepository;
import com.sampoom.user.api.factory.entity.FactoryEmployee;
import com.sampoom.user.api.factory.repository.FactoryEmployeeRepository;
import com.sampoom.user.api.factory.service.FactoryEmployeeService;
import com.sampoom.user.api.user.entity.User;
import com.sampoom.user.api.user.repository.UserRepository;
import com.sampoom.user.api.factory.entity.FactoryStatus;
import com.sampoom.user.api.factory.repository.FactoryProjectionRepository;
import com.sampoom.user.api.factory.entity.FactoryProjection;
import com.sampoom.user.api.warehouse.repository.WarehouseProjectionRepository;
import com.sampoom.user.common.entity.EmployeeStatus;
import com.sampoom.user.common.entity.Position;
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
public class UserEventHandler {

    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final FactoryEmployeeService factoryEmployeeService;
    private final FactoryEmployeeRepository factoryEmployeeRepository;
    // Warehouse/Agency 쪽도 있다면 해당 Repo 주입

    @KafkaListener(topics = "${app.topics.user-events:user-events}")
    @Transactional
    public void handle(String message, Acknowledgment ack) {
        try {
            JsonNode root = objectMapper.readTree(message);
            JsonNode payload = root.get("payload");

            Long userId = payload.get("userId").asLong();
            String email = asText(payload, "email");
            String role = asText(payload,"role");
            String userName = asText(payload, "userName");
            String workspace = asText(payload, "workspace");
            String branch = asText(payload, "branch");
            String position = asText(payload, "position");

            // User: PK 직접 주입 (Auth.id 그대로)
            userRepository.findById(userId).orElseGet(() ->
                    userRepository.save(User.builder()
                            .id(userId)
                            .userName(userName)
                            .build())
            );

            // Employee:
            if ("FACTORY".equalsIgnoreCase(workspace)) {
                factoryEmployeeService.assignFactoryEmployee(userId, userName, branch, position);
            }
            // else if ("WAREHOUSE".equalsIgnoreCase(workspace)) { ... }
            // else if ("AGENCY".equalsIgnoreCase(workspace)) { ... }
            // WAREHOUSE/AGENCY도 동일 패턴으로 분기
            ack.acknowledge(); // 수동 커밋
        } catch (Exception e) {
            log.error("UserEvent handling failed: {}", e.toString(), e);
            // 예외 시 커밋 안함 → 재시도(적절한 재시도/DLQ 설정 권장)
            throw new RuntimeException(e);
        }
    }

    private String asText(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.get(field).asText() : null;
    }
}