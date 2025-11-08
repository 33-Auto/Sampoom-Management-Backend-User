package com.sampoom.user.api.user.kafka;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sampoom.user.api.agency.repository.AgencyEmployeeRepository;
import com.sampoom.user.api.factory.repository.FactoryEmployeeRepository;
import com.sampoom.user.api.user.event.UserWarmupEvent;
import com.sampoom.user.api.warehouse.repository.WarehouseEmployeeRepository;
import com.sampoom.user.common.entity.BaseEmployeeEntity;
import com.sampoom.user.common.entity.Workspace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserWarmupPublisher {

    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper objectMapper;
    private final FactoryEmployeeRepository factoryEmpRepo;
    private final WarehouseEmployeeRepository warehouseEmpRepo;
    private final AgencyEmployeeRepository agencyEmpRepo;

    @Value("${app.topics.user-events:user-events}")
    private String userEventsTopic;

    @PostConstruct
    public void publishWarmupOnStartup() {
        try {
            var factories = toPayloads(factoryEmpRepo.findAll(),Workspace.FACTORY);
            var warehouses = toPayloads(warehouseEmpRepo.findAll(), Workspace.WAREHOUSE);
            var agencies = toPayloads(agencyEmpRepo.findAll(), Workspace.AGENCY);

            UserWarmupEvent evt = UserWarmupEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("UserSystemWarmup")
                    .occurredAt(OffsetDateTime.now().toString())
                    .factoryEmployees(factories)
                    .warehouseEmployees(warehouses)
                    .agencyEmployees(agencies)
                    .build();

            String payload = objectMapper.writeValueAsString(evt);
            kafka.send(userEventsTopic, payload);
            log.info("UserSystemWarmup 이벤트 발행 완료 (topic={})", userEventsTopic);
        } catch (Exception e) {
            log.error("UserSystemWarmup 이벤트 발행 실패", e);
        }
    }

    private List<UserWarmupEvent.UserPayload> toPayloads(
            List<? extends BaseEmployeeEntity> employees,
            Workspace workspace
    ) {
        return employees.stream()
                .map(e -> UserWarmupEvent.UserPayload.builder()
                        .userId(e.getUserId())
                        .workspace(workspace)
                        .employeeStatus(e.getStatus())
                        .createdAt(e.getCreatedAt())
                        .updatedAt(e.getUpdatedAt())
                        .version(e.getVersion())
                        .build())
                .toList();
    }
}
