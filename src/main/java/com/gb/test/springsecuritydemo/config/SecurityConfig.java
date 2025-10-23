package com.gb.test.springsecuritydemo.config;

import com.gb.test.springsecuritydemo.config.JwtAuthenticationFilter;
import com.gb.test.springsecuritydemo.config.handlers.RestAccessDeniedHandler;
import com.gb.test.springsecuritydemo.config.handlers.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
@EnableMethodSecurity // 开启“方法级安全”
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint; // 3. 注入 401 处理器
    private final RestAccessDeniedHandler restAccessDeniedHandler;         // 4. 注入 403 处理器

    // 5. 更新构造函数
    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          RestAuthenticationEntryPoint restAuthenticationEntryPoint,
                          RestAccessDeniedHandler restAccessDeniedHandler) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        this.restAccessDeniedHandler = restAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationProvider authenticationProvider) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 6. 【更新权限规则】
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/hello").permitAll() // 假设 /hello 还是公开的
                        // 7. 【新规则】/api/admin/ 下的所有请求，都必须有 "ADMIN" 角色
                        // 注意: .hasRole("ADMIN") 会自动寻找 "ROLE_ADMIN"
                        //.requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // 8. 其他所有请求都需要登录
                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 9. 【新配置】注册我们的全局异常处理器
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint) // 配置 401 处理器
                        .accessDeniedHandler(restAccessDeniedHandler)           // 配置 403 处理器
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
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


    public static void main(String[] args) {
        String encode = new BCryptPasswordEncoder().encode("123");
        System.out.println(encode);
    }

}