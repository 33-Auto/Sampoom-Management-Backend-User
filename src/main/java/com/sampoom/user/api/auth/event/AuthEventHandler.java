package com.sampoom.user.api.auth.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sampoom.user.api.auth.service.AuthUserProjectionService;
import com.sampoom.user.common.exception.InternalServerErrorException;
import com.sampoom.user.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.KafkaException;
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
        }
        catch (JsonProcessingException ex) {
            log.error("[AuthUserEventHandler] 이벤트 포맷 오류: {}", message, ex);
            throw new InternalServerErrorException(ErrorStatus.INVALID_EVENT_FORMAT);
        }
        catch (KafkaException ex) {
            log.error("[AuthUserEventHandler] Kafka 통신 실패", ex);
            throw new InternalServerErrorException(ErrorStatus.FAILED_CONNECTION_KAFKA);
        }
        catch (Exception e) {
            log.error("[AuthUserEventHandler] Kafka 이벤트 처리 실패", e);
            throw new InternalServerErrorException(ErrorStatus.EVENT_PROCESSING_FAILED);
        }
    }
}