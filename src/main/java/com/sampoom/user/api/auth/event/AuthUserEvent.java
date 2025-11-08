package com.sampoom.user.api.auth.event;

import com.sampoom.user.common.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class AuthUserEvent {
    private String eventId;
    private String eventType;      // "AuthUserSignedUp","AuthUserUpdated"
    private Long version;
    private String occurredAt;
    private Payload payload;

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Payload {
        // User(Auth)
        @NotNull
        private Long userId;
        @NotNull
        private String email;
        @NotNull
        private Role role;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime deletedAt;
    }
}
