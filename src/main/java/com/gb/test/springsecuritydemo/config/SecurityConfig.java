package com.gb.test.springsecuritydemo.config;

import com.gb.test.springsecuritydemo.config.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity // (1) 确保这个注解存在
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter; // (2) 只需要注入过滤器

    // (3) 在构造函数中只注入你真正需要作为*字段*的 Bean
    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /**
     * (4) 核心的安全过滤器链
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationProvider authenticationProvider) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 禁用 CSRF
                .authorizeHttpRequests(auth -> auth
                        // 允许访问登录和注册接口
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/register").permitAll() // 假设你也有注册
                        // 其他所有请求都需要认证
                        .anyRequest().authenticated()
                )
                // (5) 设置 Session 管理为无状态
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // (6) 关联我们下面定义的 AuthenticationProvider
                .authenticationProvider(authenticationProvider)
                // (7) 将 JWT 过滤器添加到链中
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // 禁用默认登录页
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }

    /**
     * (8) 密码编码器
     * 必须将其声明为 Bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * (9) 认证管理器
     * 登录接口会用到
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * (10) 认证提供者
     * * 这是避免循环依赖的关键：
     * 它需要的 UserDetailsService 和 PasswordEncoder
     * 不是通过类字段注入的，而是通过 *方法参数* 注入的。
     * Spring 会自动从容器中找到这两个 Bean 并传进来。
     */
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // (11) 告诉 provider 使用哪个 UserDetailsService
        authProvider.setUserDetailsService(userDetailsService);
        // (12) 告诉 provider 使用哪个 PasswordEncoder
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }
}