package com.sampoom.user.api.user.entity;

import com.sampoom.user.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseTimeEntity {

    @Id
    private Long id;            // Auth.id

    @Column(nullable = false, length = 50)
    private String userName;    // 사용자 이름

    // 더티 체킹: 회원정보 수정용 setter만 공개 (다른 필드는 숨김)
    public void setUserName(String userName) {
        this.userName = userName;
    }
}
