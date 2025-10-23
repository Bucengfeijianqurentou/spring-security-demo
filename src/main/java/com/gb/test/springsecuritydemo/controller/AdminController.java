package com.gb.test.springsecuritydemo.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/hello")
    @PreAuthorize("hasRole('ADMIN')") // <-- 只有 "ROLE_ADMIN" 的用户能访问
    public String adminHello() {
        return "你好, [管理员]!";
    }

    @GetMapping("/hello-user")
    @PreAuthorize("hasRole('USER')") // <-- 只有 "ROLE_USER" 的用户能访问
    public String userHello() {
        return "你好, [普通用户]!";
    }
}