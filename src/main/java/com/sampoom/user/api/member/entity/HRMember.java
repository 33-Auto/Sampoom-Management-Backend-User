package com.sampoom.user.api.member.entity;

import com.sampoom.user.common.entity.*;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hr_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class HRMember extends BaseMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hr_member_id")
    private Long id;  // 인사 부서 직원 ID
}
