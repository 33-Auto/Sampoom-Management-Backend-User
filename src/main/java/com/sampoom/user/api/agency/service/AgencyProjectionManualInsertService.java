package com.sampoom.user.api.agency.service;

import com.sampoom.user.api.agency.entity.AgencyProjection;
import com.sampoom.user.api.agency.entity.VendorStatus;
import com.sampoom.user.api.agency.repository.AgencyProjectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgencyProjectionManualInsertService {

    private final AgencyProjectionRepository agencyProjectionRepository;

    /**
     * HR 데이터 없이 직접 projection 테이블을 복원 (Kafka 이벤트 불필요)
     */
    @Transactional
    public void restoreDefaultAgencies() {
        var now = OffsetDateTime.now();

        List<AgencyProjection> agencies = List.of(
                AgencyProjection.builder()
                        .agencyId(154L)
                        .agencyCode("AGC-001")
                        .name("부산 대리점")
                        .businessNumber("120-311-12313")
                        .ceoName("박상구")
                        .address("부산 동구 중앙대로 206")
                        .status(VendorStatus.ACTIVE)
                        .deleted(false)
                        .version(1L)
                        .lastEventId(UUID.randomUUID())
                        .sourceUpdatedAt(now)
                        .updatedAt(now)
                        .build(),

                AgencyProjection.builder()
                        .agencyId(155L)
                        .agencyCode("AGC-002")
                        .name("광주 대리점")
                        .businessNumber("123-311-12313")
                        .ceoName("이수진")
                        .address("광주 북구 무등로 235")
                        .status(VendorStatus.ACTIVE)
                        .deleted(false)
                        .version(1L)
                        .lastEventId(UUID.randomUUID())
                        .sourceUpdatedAt(now)
                        .updatedAt(now)
                        .build(),

                AgencyProjection.builder()
                        .agencyId(156L)
                        .agencyCode("AGC-003")
                        .name("강원 대리점")
                        .businessNumber("124-311-12313")
                        .ceoName("이수강")
                        .address("강원 춘천시 공지로 591")
                        .status(VendorStatus.ACTIVE)
                        .deleted(false)
                        .version(1L)
                        .lastEventId(UUID.randomUUID())
                        .sourceUpdatedAt(now)
                        .updatedAt(now)
                        .build(),

                AgencyProjection.builder()
                        .agencyId(157L)
                        .agencyCode("AGC-004")
                        .name("서울 대리점")
                        .businessNumber("125-311-12313")
                        .ceoName("김민수")
                        .address("서울 용산구 한강대로 405")
                        .status(VendorStatus.ACTIVE)
                        .deleted(false)
                        .version(1L)
                        .lastEventId(UUID.randomUUID())
                        .sourceUpdatedAt(now)
                        .updatedAt(now)
                        .build()
        );

        agencyProjectionRepository.saveAll(agencies);
        log.info("✅ AgencyProjection 테이블 초기 데이터 {}건 복원 완료", agencies.size());
    }
}
