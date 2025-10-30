package com.sampoom.user.api.user.dto.request;

import com.sampoom.user.common.entity.Position;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {
    private String userName;
}
