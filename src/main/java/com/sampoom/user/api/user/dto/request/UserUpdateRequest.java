package com.sampoom.user.api.user.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {
    private String userName;
    private String position;
    private String workspace;
    private String branch;
}
