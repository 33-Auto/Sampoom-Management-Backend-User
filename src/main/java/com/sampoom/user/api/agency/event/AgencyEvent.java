package com.sampoom.user.api.agency.event;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgencyEvent {
    private String eventId;        // UUID
    private String eventType;      // AgencyCreated / AgencyUpdated / AgencyDeleted
    private Long version;
    private String occurredAt;
    private Payload payload;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Payload {
        private Long agencyId;
        private String name;
        private String address;
        private String status;
        private Boolean deleted;
    }
}
