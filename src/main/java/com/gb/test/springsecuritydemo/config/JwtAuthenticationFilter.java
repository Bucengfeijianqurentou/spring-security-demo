package com.gb.test.springsecuritydemo.config;

import com.gb.test.springsecuritydemo.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component // 1. 把它也声明为一个 Spring 组件
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // 2. 通过构造函数注入
    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 3. 从请求头中获取 "Authorization"
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 4. 检查 Header 是否存在，以及是否以 "Bearer " 开头
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // 如果不是 JWT，放行，让后续过滤器处理
            return;
        }

        // 5. 提取 JWT 令牌 (去掉 "Bearer " 前缀)
        jwt = authHeader.substring(7);

        try {
            // 6. 从令牌中解析出用户名
            username = jwtUtil.extractUsername(jwt);

            // 7. 检查用户名不为空，且 *当前的安全上下文中没有* 认证信息
            //    (SecurityContextHolder.getContext().getAuthentication() == null)
            //    这个检查是为了防止在一次请求中重复认证
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 8. 根据用户名，从数据库加载 UserDetails
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // 9. 验证令牌是否有效（用户名匹配且未过期）
                if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {

                    // 10. **关键步骤**：如果令牌有效，创建一个认证令牌
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, // principal (当事人，即 UserDetails)
                            null,        // credentials (凭证，JWT 模式下不需要)
                            userDetails.getAuthorities() // 用户的权限
                    );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // 11. **将认证令牌设置到 Spring Security 的上下文中**
                    //    从这一刻起，Spring Security 就知道这个用户已经过认证了
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            // 12. 无论是否认证成功，都放行请求
            //    如果认证成功，后续的过滤器会看到 SecurityContext 中有认证信息
            //    如果失败（比如令牌无效），SecurityContext 中没有认证信息，后续的过滤器（如 AuthorizationFilter）会拒绝访问
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            // 如果 JWT 解析或验证失败（如过期、签名错误）
            // 我们可以直接在这里处理异常，比如返回 401 或 403
            // 为简单起见，我们先放行，让后续的安全机制处理（它会因为没有认证信息而拒绝）
            // 在生产中，你可能想在这里直接返回 response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Token");
            filterChain.doFilter(request, response);
        }
    }
}