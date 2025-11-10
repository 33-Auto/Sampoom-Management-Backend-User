package com.sampoom.user.api.member.entity;

import com.sampoom.user.common.entity.*;
import jakarta.persistence.*;
import lombok.*;

import static com.sampoom.user.common.entity.Workspace.HR;
import static com.sampoom.user.common.entity.Workspace.PURCHASE;

@Entity
@Table(name = "purchase_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PurchaseMember extends BaseMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_member_id")
    private Long id;  // 구매 부서 직원 ID

    public static PurchaseMember create(Long userId, Position position) {
        PurchaseMember m = new PurchaseMember();
        m.setUserId(userId);
        m.updatePosition(position);
        return m;
    }
}
