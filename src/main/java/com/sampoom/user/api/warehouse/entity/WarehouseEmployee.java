package com.sampoom.user.api.warehouse.entity;

import com.sampoom.user.common.entity.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "warehouse_employee")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WarehouseEmployee extends BaseEmployeeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "warehouse_employee_id")
    private Long id;  // 창고-직원 ID

    @Column(nullable = false)
    private Long warehouseId;  // 창고 ID
}
