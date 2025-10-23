package com.sampoom.user.api.user.entity;

import com.sampoom.user.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 20)
    private String role;   // ROLE_USER, ROLE_ADMIN 등 스프링 시큐리티 기본 role

    @Column(length = 50)
    private String workspace;   // 직장 분류(대리점, 창고, 공장)

    @Column(length = 50)
    private String branch;      // 지점명

    @Column(nullable = false, length = 50)
    private String userName;        // 사용자 이름

    @Column(length = 50)
    private String position;    // 사용자 직책

    // role 기본값 설정
    @PrePersist
    public void prePersist() {
        if (this.role == null) {
            this.role = "ROLE_USER";
        }
    }

    // 더티 체킹: 회원정보 수정용 setter만 공개 (다른 필드는 숨김)
    public void setUserName(String userName) { this.userName = userName; }
    public void setPosition(String position) { this.position = position; }
    public void setWorkspace(String workspace) { this.workspace = workspace; }
    public void setBranch(String branch) { this.branch = branch; }
}
