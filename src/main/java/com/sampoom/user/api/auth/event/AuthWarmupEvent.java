package com.sampoom.user.api.auth.event;

import com.sampoom.user.common.entity.Role;
import com.sampoom.user.common.entity.Workspace;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthWarmupEvent {
    private String eventId;
    private String eventType; // 항상 "AuthSystemWarmup"
    private String occurredAt;
    private List<AuthUserPayload> payload;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthUserPayload {
        private Long userId;
        private String email;
        private Role role;
        private Workspace workspace;
        private Long version;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
