package com.sampoom.user.api.user.dto.request;

import com.sampoom.user.common.entity.EmployeeStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeStatusRequest {
    private EmployeeStatus employeeStatus;
}
