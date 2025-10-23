package com.gb.test.springsecuritydemo.controller;

import com.gb.test.springsecuritydemo.model.LoginRequest;
import com.gb.test.springsecuritydemo.model.LoginResponse;
import com.gb.test.springsecuritydemo.utils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth") // 路由前缀
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    // 1. 注入我们需要的 Bean
    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 登录接口
     * @param request 包含用户名和密码的 DTO
     * @return 成功则返回 JWT 令牌，失败则返回错误信息
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // 2. **执行认证**
            //    这会触发 Spring Security 的认证流程
            //    它会去调用我们配置的 AuthenticationProvider -> UserDetailsService -> PasswordEncoder
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );

            // 3. **如果认证成功** (即上一行没有抛出异常)
            //    我们就使用 JwtUtil 来为这个用户生成一个令牌
            final String token = jwtUtil.generateToken(request.username());

            // 4. 返回成功的响应，包含令牌
            return ResponseEntity.ok(new LoginResponse(token));

        } catch (BadCredentialsException e) {
            // 5. **如果认证失败** (比如密码错误)
            //    authenticationManager.authenticate 会抛出 BadCredentialsException
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        } catch (Exception e) {
            // 6. 处理其他可能的认证异常 (比如用户被锁定、账户过期等)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    // 你也可以在这里添加 /register 接口...
}
