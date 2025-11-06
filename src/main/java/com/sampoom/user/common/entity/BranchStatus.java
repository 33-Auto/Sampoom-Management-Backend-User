package com.sampoom.user.common.entity;

import lombok.Getter;

@Getter
public enum BranchStatus {
    ACTIVE("활성"),
    INACTIVE("비활성");

    private final String description;

    BranchStatus(String description) {
        this.description = description;
    }
}
