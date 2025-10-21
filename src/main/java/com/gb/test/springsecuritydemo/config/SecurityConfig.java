package com.gb.test.springsecuritydemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 1. 修改 /hello：不再是 permitAll()，而是需要认证
                        .requestMatchers("/hello").authenticated()
                        // 2. 其他所有请求也都需要认证
                        .anyRequest().authenticated()
                )
                // 3. 启用表单登录功能
                .formLogin(formLogin ->
                        formLogin
                                // .loginPage("/login") // 你可以指定自定义的登录页面
                                .permitAll() // 确保 Spring Security 相关的登录端点是可访问的
                );

        return http.build();
    }


    /**
     * 定义一个 PasswordEncoder Bean
     * 它会使用 BCrypt 强哈希算法来加密密码
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public static void main(String[] args) {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode("123456");
        System.out.println(encode);
    }

}
