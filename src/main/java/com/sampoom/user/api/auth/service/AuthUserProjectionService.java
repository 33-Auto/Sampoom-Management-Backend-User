package com.sampoom.user.api.auth.service;

import com.sampoom.user.api.auth.event.AuthUserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthUserProjectionService {

    private final AuthUserProjectionUpdater updater;

    @Transactional
    public void apply(AuthUserEvent e) {
        updater.validateEvent(e);

        switch (e.getEventType()) {
            case "UserSignedUp", "AuthUserUpdated" -> updater.upsert(e);
            case "UserDeactivated" -> updater.softDelete(e);
            default -> log.info("[AuthUserProjection] 규정하지 않은 eventType: {}", e.getEventType());
        }
    }
}
