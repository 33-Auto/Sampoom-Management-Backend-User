package com.sampoom.user.api.user.internal.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    private Long userId;       // Auth에서 생성한 userId
    private String userName;   // 사용자 이름
    private String workspace;  // 근무지(대리점, 창고, 공장 등)
    private String branch;     // 지점명
    private String position;   // 직책
}