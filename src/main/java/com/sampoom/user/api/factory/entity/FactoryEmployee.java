package com.sampoom.user.api.factory.entity;

import com.sampoom.user.common.entity.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "factory_employee")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FactoryEmployee extends BaseEmployeeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "factory_employee_id")
    private Long id;  // 공장-직원 ID
    @Column(nullable = false)
    private Long factoryId;  // 공장 ID
}