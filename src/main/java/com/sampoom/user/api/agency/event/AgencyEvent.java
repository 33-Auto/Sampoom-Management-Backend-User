package com.sampoom.user.api.agency.event;

import com.sampoom.user.api.agency.entity.VendorStatus;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgencyEvent {
    private String eventId;        // UUID
    private String eventType;      // VendorCreated / VendorUpdated / VendorDeleted
    private Long version;
    private String occurredAt;
    private Payload payload;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Payload {
        private Long vendorId;
        private String vendorCode;
        private String vendorName;
        private String address;
        private Double latitude;
        private Double longitude;
        private String businessNumber;
        private String ceoName;
        private String status;
        private Boolean deleted;
    }
}
