package com.sampoom.user.api.factory.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FactoryEvent {
    private String eventId;
    private String eventType;
    private Long version;
    private String occurredAt;
    private Payload payload;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payload {
        private Long branchId;
        private String branchCode;
        private String branchName;
        private String address;
        private Double latitude;
        private Double longitude;
        private String status;       // ACTIVE / INACTIVE 등
        private Boolean deleted;     // 논리삭제 여부;
    }
}