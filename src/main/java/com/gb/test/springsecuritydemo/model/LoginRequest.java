package com.gb.test.springsecuritydemo.model;

// DTO: LoginRequest
// record 是 Java 16+ 的特性，它会自动生成构造函数、getter、equals、hashCode 和 toString
// 如果你用的是旧版 Java，可以创建一个普通的 POJO 类
public record LoginRequest(String username, String password) {
}