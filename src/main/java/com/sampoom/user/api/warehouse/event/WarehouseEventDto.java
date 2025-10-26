package com.sampoom.user.api.warehouse.event;

import com.sampoom.user.api.warehouse.entity.WarehouseStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;

@Getter
@Setter
@ToString
public class WarehouseEventDto {
    private Long id;
    private String name;
    private String address;
    private WarehouseStatus status;
    private Long version;
    private OffsetDateTime sourceUpdatedAt;
}
