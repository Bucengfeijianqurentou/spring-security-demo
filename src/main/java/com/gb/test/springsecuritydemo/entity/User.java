package com.gb.test.springsecuritydemo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

// 1. 不再 "implements UserDetails"
@Entity
@Table(name = "users") // 确保你的表名是 'users'
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String password;

    // 2. 新增一个 "role" 字段
    private String role;

}