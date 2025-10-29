package com.sampoom.user.api.auth.event;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthUserEvent {
    private String eventId;
    private String eventType;
    private Long version;
    private String occurredAt;
    private Payload payload;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payload {
        private Long userId;
        private String email;
        private String role;
        private Boolean deleted;
    }
}
