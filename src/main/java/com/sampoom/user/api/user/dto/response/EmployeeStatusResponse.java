package com.sampoom.user.api.user.dto.response;

import com.sampoom.user.common.entity.EmployeeStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeStatusResponse {
    private Long userId;
    private String userName;
    private EmployeeStatus employeeStatus;
}
