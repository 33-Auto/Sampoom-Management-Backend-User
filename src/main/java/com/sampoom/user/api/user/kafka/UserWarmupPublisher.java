package com.sampoom.user.api.user.kafka;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sampoom.user.api.agency.repository.AgencyEmployeeRepository;
import com.sampoom.user.api.member.repository.*;
import com.sampoom.user.api.user.event.UserWarmupEvent;
import com.sampoom.user.common.entity.BaseMemberEntity;
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
    private final ProductionMemberRepository prodRepo;
    private final InventoryMemberRepository invenRepo;
    private final AgencyEmployeeRepository agencyRepo;
    private final PurchaseMemberRepository purchaseRepo;
    private final SalesMemberRepository salesRepo;
    private final MDMemberRepository mdRepo;
    private final HRMemberRepository hrRepo;

    @Value("${app.topics.user-events:user-events}")
    private String userEventsTopic;

    @PostConstruct
    public void publishWarmupOnStartup() {
        try {
            var prodMembers = toPayloads(prodRepo.findAll(), Workspace.PRODUCTION);
            var invenMembers = toPayloads(invenRepo.findAll(), Workspace.INVENTORY);
            var agencyMembers = toPayloads(agencyRepo.findAll(), Workspace.AGENCY);
            var purchaseMembers = toPayloads(purchaseRepo.findAll(), Workspace.PURCHASE);
            var salesMembers = toPayloads(salesRepo.findAll(), Workspace.SALES);
            var mdMembers = toPayloads(mdRepo.findAll(), Workspace.MD);
            var hrMembers = toPayloads(hrRepo.findAll(), Workspace.HR);

            UserWarmupEvent evt = UserWarmupEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("UserSystemWarmup")
                    .occurredAt(OffsetDateTime.now().toString())
                    .prodMembers(prodMembers)
                    .invenMembers(invenMembers)
                    .agencyMembers(agencyMembers)
                    .purchaseMembers(purchaseMembers)
                    .salesMembers(salesMembers)
                    .hrMembers(hrMembers)
                    .mdMembers(mdMembers)
                    .build();

            String payload = objectMapper.writeValueAsString(evt);
            kafka.send(userEventsTopic, payload);
            log.info("UserSystemWarmup 이벤트 발행 완료 (topic={})", userEventsTopic);
        } catch (Exception e) {
            log.error("UserSystemWarmup 이벤트 발행 실패", e);
        }
    }

    private List<UserWarmupEvent.UserPayload> toPayloads(
            List<? extends BaseMemberEntity> members,
            Workspace workspace
    ) {
        return members.stream()
                .map(e -> UserWarmupEvent.UserPayload.builder()
                        .userId(e.getUserId())
                        .employeeStatus(e.getStatus())
                        .createdAt(e.getCreatedAt())
                        .updatedAt(e.getUpdatedAt())
                        .version(e.getVersion())
                        .build())
                .toList();
    }
}
