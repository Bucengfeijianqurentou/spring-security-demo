package com.gb.test.springsecuritydemo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List; // 假设我们稍后会处理权限

@Entity
@Table(name = "users") // 假设你的表名叫 'users'
public class User implements UserDetails {

    @Id
    private Long id;
    private String username;
    private String password;

    // ... 其他字段，如 email, nickname 等 ...

    // --- UserDetails 接口要求实现的方法 ---

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public String getPassword() {
        // 非常重要：这里返回的必须是数据库中存储的 *加密后* 的密码
        return this.password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 这里应该返回用户的权限列表（如 "ROLE_ADMIN", "ROLE_USER"）
        // 我们暂时先返回一个空列表
        return List.of();
    }

    // --- 其他几个方法（我们暂时设为 true）---

    @Override
    public boolean isAccountNonExpired() {
        return true; // 账户未过期
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 账户未锁定
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 凭证未过期
    }

    @Override
    public boolean isEnabled() {
        return true; // 账户已启用
    }
}
