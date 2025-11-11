package com.sampoom.user.api.member.entity;

import com.sampoom.user.common.entity.*;
import jakarta.persistence.*;
import lombok.*;

import static com.sampoom.user.common.entity.Workspace.HR;
import static com.sampoom.user.common.entity.Workspace.PRODUCTION;

@Entity
@Table(name = "production_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProductionMember extends BaseMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "production_member_id")
    private Long id;  // 생산 부서 직원 ID

    public static ProductionMember create(Long userId, Position position) {
        ProductionMember m = new ProductionMember();
        m.setUserId(userId);
        m.updatePosition(position);
        return m;
    }
}