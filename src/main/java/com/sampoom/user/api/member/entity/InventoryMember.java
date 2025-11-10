package com.sampoom.user.api.member.entity;

import com.sampoom.user.common.entity.*;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventory_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class InventoryMember extends BaseMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_member_id")
    private Long id;  // 재고 부서 직원 ID
}
