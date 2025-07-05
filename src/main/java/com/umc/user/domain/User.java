package com.umc.user.domain;

import com.umc.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String password;  // BCrypt 해시 저장

}
