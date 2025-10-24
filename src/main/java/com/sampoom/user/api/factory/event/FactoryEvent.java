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
        private Long factoryId;
        private String name;
        private String address;
        private String status;
        private Boolean deleted;
    }
}