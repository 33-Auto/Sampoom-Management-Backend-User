package com.sampoom.user.api.user.event;

import com.sampoom.user.common.entity.EmployeeStatus;
import com.sampoom.user.common.entity.Workspace;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedEvent {
    private String eventId;
    private String eventType;      // "UserCreated"
    private Long version;
    private String occurredAt;
    private Payload payload;

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Payload {
        // User(Auth)
        @NotNull
        private Long userId;
        @NotNull
        private Workspace workspace;
        @NotNull
        private EmployeeStatus employeeStatus;

        private LocalDateTime updatedAt;    // createdAt이지만 dto 통일 위해
    }
}
