package com.sampoom.user.api.member.entity;
import com.sampoom.user.common.entity.*;
import jakarta.persistence.*;
import lombok.*;

import static com.sampoom.user.common.entity.Workspace.HR;
import static com.sampoom.user.common.entity.Workspace.SALES;

@Entity
@Table(name = "sales_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SalesMember extends BaseMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sales_member_id")
    private Long id;  // 영업 부서 직원 ID

    public static SalesMember create(Long userId, Position position) {
        SalesMember m = new SalesMember();
        m.setUserId(userId);
        m.updatePosition(position);
        return m;
    }
}
