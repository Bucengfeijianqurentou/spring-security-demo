package com.gb.test.springsecuritydemo.repository;

import com.gb.test.springsecuritydemo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// 我们让它继承 JpaRepository，并指定它管理的实体是 User，主键类型是 Long
public interface UserRepository extends JpaRepository<User, Long> {

    // 只需要按照这个格式定义方法名
    // Spring Data JPA 就会自动帮我们实现一个 "SELECT * FROM users WHERE username = ?" 的查询
    Optional<User> findByUsername(String username);
}