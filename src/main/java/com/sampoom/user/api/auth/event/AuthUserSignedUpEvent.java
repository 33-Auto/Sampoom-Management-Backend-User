package com.sampoom.user.api.auth.event;

import com.sampoom.user.common.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class AuthUserSignedUpEvent {
    private String eventId;
    private String eventType;      // "UserSignedUp"
    private Long version;
    private String occurredAt;     // ISO-8601
    private Payload payload;

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Payload {
        // User(Auth)
        private Long userId;
        private String email;
        private Role role;
        protected LocalDateTime createdAt;
    }
}
