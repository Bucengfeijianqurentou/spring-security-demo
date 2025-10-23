### 项目核心组件概览

为了方便理解，我们先明确一下你项目中各个“演员”的职责：

* **`SecurityConfig` (配置总管)**：配置所有规则。
    * `filterChain` (Bean)：定义了过滤器链条和 URL 权限（比如 `/api/admin/**` 需要 "ADMIN" 角色）。
    * `@EnableMethodSecurity` (注解)：开启了 `@PreAuthorize` 功能。
    * `exceptionHandling(...)`：注册了下面两个自定义的异常处理器。
* **`JwtAuthenticationFilter` (门卫)**：
    * `extends OncePerRequestFilter`。
    * **职责**：拦截*所有*请求，检查 `Authorization` 头中的 JWT 令牌。如果令牌有效，就解析它，加载用户信息，并将其放入 `SecurityContextHolder`。
* **`MyUserDetailsService` (人事档案室)**：
    * **职责**：被调用时，根据用户名（如 "admin"）去数据库 `UserRepository` 查询 `User` 实体，然后将其**转换**为 Spring Security 认识的 `UserDetails` 对象（包含权限）。
* **`AuthController` (登录前台)**：
    * **职责**：只处理 `/api/auth/login`。调用 `AuthenticationManager` 来验证用户名密码，如果成功，就用 `JwtUtil` 生成令牌并返回。
* **`AdminController` (受保护的办公室)**：
    * **职责**：包含受保护的业务接口。
    * `@PreAuthorize("hasRole('ADMIN')")`：在方法执行前，检查当前用户是否有 "ADMIN" 角色。
* **`RestAuthenticationEntryPoint` (401 处理器)**：
    * **职责**：当一个**未登录**用户试图访问受保护资源时，由 Spring Security 内部的 `ExceptionTranslationFilter` 调用，返回你定义的 401 JSON。
* **`RestAccessDeniedHandler` (403 处理器)**：
    * **职责**：当一个**已登录**但**权限不足**的用户访问受保护资源时，由 `ExceptionTranslationFilter` 调用，返回你定义的 403 JSON。

---

### 各种情况下的完整执行流程

#### 场景一：管理员登录 (成功)

1.  **[请求]**：Postman (或前端) 发起 `POST /api/auth/login`，请求体为 `{"username": "admin", "password": "123456"}`。
2.  **[进入过滤器链]**：请求进入 Spring Security 的过滤器链。
3.  **[`JwtAuthenticationFilter`]**：`doFilterInternal` 执行。
    * 它检查 `Authorization` 头，发现是 `null`（登录请求不带 Token）。
    * 它什么也不做，直接调用 `filterChain.doFilter(request, response)` 放行。
4.  **[Spring Security (授权)]**：请求到达内部的 `AuthorizationFilter` (授权过滤器)。
    * 它检查 `SecurityConfig` 中的规则：`.requestMatchers("/api/auth/login").permitAll()`。
    * **匹配成功**！请求被允许通过。
5.  **[Spring MVC]**：请求被分发到你的 `AuthController`。
6.  **[`AuthController`]**：`login()` 方法被调用。
    * `authenticationManager.authenticate(...)` 被执行。
7.  **[Spring Security (认证)]**：`AuthenticationManager` 将任务委托给我们配置的 `DaoAuthenticationProvider`。
8.  **[`DaoAuthenticationProvider`]**：调用 `MyUserDetailsService.loadUserByUsername("admin")`。
9.  **[`MyUserDetailsService`]**：
    * 调用 `userRepository.findByUsername("admin")`，从数据库获取 `User` 实体（包含加密的密码和 `role="ROLE_ADMIN"`）。
    * **执行转换**：`new org.springframework.security.core.userdetails.User("admin", "...", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))`。
    * 返回这个 `UserDetails` 对象。
10. **[`DaoAuthenticationProvider`]**：
    * 获取到 `UserDetails`。
    * 调用 `passwordEncoder.matches("123456", "...")` 比较密码。
    * **密码匹配成功**。认证通过。
11. **[`AuthController`]**：`authenticate()` 方法顺利返回（没抛异常）。
    * `try` 块继续执行。
    * `jwtUtil.generateToken("admin")` 被调用，生成 JWT 令牌 (`TOKEN_ADMIN`)。
12. **[响应]**：`AuthController` 返回 `ResponseEntity.ok(new LoginResponse(TOKEN_ADMIN))`。前端收到 `200 OK` 和包含 Token 的 JSON。

---

#### 场景二：访问公开接口 (任何人)

1.  **[请求]**：浏览器发起 `GET /hello`。
2.  **[进入过滤器链]**：...
3.  **[`JwtAuthenticationFilter`]**：`doFilterInternal` 执行。
    * `Authorization` 头为 `null`。
    * 直接 `filterChain.doFilter(request, response)` 放行。
4.  **[Spring Security (授权)]**：`AuthorizationFilter` 检查规则。
    * 匹配到 `.requestMatchers("/hello").permitAll()`。
    * **匹配成功**！请求被允许通过。
5.  **[Spring MVC]**：请求被分发到 `HelloController`。
6.  **[响应]**：`HelloController` 返回 `200 OK` 和 "Hello, Spring Security!"。

---

#### 场景三：未登录访问受保护资源 (触发 401)

1.  **[请求]**：用户**不带 Token** 访问 `GET /api/admin/hello`。
2.  **[进入过滤器链]**：...
3.  **[`JwtAuthenticationFilter`]**：`doFilterInternal` 执行。
    * `Authorization` 头为 `null`。
    * 直接 `filterChain.doFilter(request, response)` 放行。
    * **此时 `SecurityContextHolder` 还是空的 (匿名状态)**。
4.  **[Spring Security (授权)]**：`AuthorizationFilter` 检查规则。
    * 匹配到 `.requestMatchers("/api/admin/**").hasRole("ADMIN")`。
    * `AuthorizationFilter` 查看 `SecurityContextHolder`，发现是**匿名用户**。
    * 规则要求 "ADMIN"，但用户是"匿名"。
    * `AuthorizationFilter` **抛出一个 `AuthenticationException` (认证异常)**。
5.  **[Spring Security (异常)]**：该异常被 Spring Security 内部的 `ExceptionTranslationFilter` (异常捕获总管) 捕获。
6.  **[`ExceptionTranslationFilter`]**：
    * 捕获到 `AuthenticationException`。
    * 检查发现用户是**匿名的**。
    * **决定调用 `AuthenticationEntryPoint`**。
7.  **【执行处理器】**：调用你注入的 `RestAuthenticationEntryPoint.commence()` 方法。
8.  **[`RestAuthenticationEntryPoint`]**：
    * 设置响应状态为 `401 Unauthorized`。
    * 创建 `ResultVO.fail(ResponseCodeEnum.UNAUTHORIZED, ...)`。
    * 使用 `ObjectMapper` 将 `ResultVO` 序列化为 JSON 并写入响应。
9.  **[响应]**：前端收到 `401` 和你定义的 JSON 错误：`{"success":false, "code":401, "message":"未认证...", ...}`。

---

#### 场景四：权限不足访问受保护资源 (触发 403)

1.  **[请求]**：一个**普通用户** (已登录，角色为 `ROLE_USER`) 带着他的 `TOKEN_USER` 访问 `GET /api/admin/hello`。
2.  **[进入过滤器链]**：...
3.  **[`JwtAuthenticationFilter`]**：`doFilterInternal` 执行。
    * `Authorization` 头存在，`TOKEN_USER` 被提取。
    * `jwtUtil.extractUsername(TOKEN_USER)` 返回 "user"。
    * 调用 `MyUserDetailsService.loadUserByUsername("user")`。
4.  **[`MyUserDetailsService`]**：从数据库查到 "user"，并返回包含 `ROLE_USER` 权限的 `UserDetails` 对象。
5.  **[`JwtAuthenticationFilter`]**：
    * `jwtUtil.validateToken` 验证通过。
    * 创建 `UsernamePasswordAuthenticationToken` (包含 `ROLE_USER` 权限)。
    * **`SecurityContextHolder.getContext().setAuthentication(...)` 被调用**。
    * **此时，该用户在本次请求中被视为 "已认证" 状态，身份是 `ROLE_USER`**。
    * 调用 `filterChain.doFilter(request, response)` 放行。
6.  **[Spring Security (授权)]**：`AuthorizationFilter` 检查规则。
    * 匹配到 `.requestMatchers("/api/admin/**").hasRole("ADMIN")`。
    * `AuthorizationFilter` 查看 `SecurityContextHolder`，发现用户**已认证**，但权限是 `ROLE_USER`。
    * 规则要求 "ADMIN"，但用户只有 "USER"。
    * `AuthorizationFilter` **抛出一个 `AccessDeniedException` (访问被拒绝异常)**。
7.  **[Spring Security (异常)]**：`ExceptionTranslationFilter` (异常捕获总管) 捕获该异常。
8.  **[`ExceptionTranslationFilter`]**：
    * 捕获到 `AccessDeniedException`。
    * 检查发现用户是**已认证的** (非匿名)。
    * **决定调用 `AccessDeniedHandler`**。
9.  **【执行处理器】**：调用你注入的 `RestAccessDeniedHandler.handle()` 方法。
10. **[`RestAccessDeniedHandler`]**：
    * 设置响应状态为 `403 Forbidden`。
    * 创建 `ResultVO.fail(ResponseCodeEnum.FORBIDDEN, ...)`。
    * 将 `ResultVO` 序列化为 JSON 并写入响应。
11. **[响应]**：前端收到 `403` 和你定义的 JSON 错误：`{"success":false, "code":403, "message":"权限不足...", ...}`。

---

#### 场景五：管理员访问受保护资源 (成功)

1.  **[请求]**：一个**管理员** (已登录，角色为 `ROLE_ADMIN`) 带着 `TOKEN_ADMIN` 访问 `GET /api/admin/hello`。
2.  **[进入过滤器链]**：...
3.  **[`JwtAuthenticationFilter`]**：`doFilterInternal` 执行。
    * `TOKEN_ADMIN` 被解析，用户名是 "admin"。
    * `MyUserDetailsService` 被调用，返回包含 `ROLE_ADMIN` 权限的 `UserDetails` 对象。
    * **`SecurityContextHolder` 被成功设置** (包含 `ROLE_ADMIN` 权限)。
    * `filterChain.doFilter(request, response)` 放行。
4.  **[Spring Security (授权)]**：`AuthorizationFilter` 检查规则。
    * 匹配到 `.requestMatchers("/api/admin/**").hasRole("ADMIN")`。
    * `AuthorizationFilter` 查看 `SecurityContextHolder`，发现用户拥有 `ROLE_ADMIN` 权限。
    * **规则满足**！请求被允许通过。
5.  **[Spring MVC]**：请求被分发到 `AdminController`。
6.  **[Spring Security (方法)]**：在 `adminHello()` 方法执行**之前**，`@EnableMethodSecurity` 激活的拦截器开始工作。
    * 它检查 `@PreAuthorize("hasRole('ADMIN')")` 注解。
    * 它再次检查 `SecurityContextHolder`，发现用户拥有 `ROLE_ADMIN` 权限。
    * **权限满足**！方法被允许执行。
7.  **[`AdminController`]**：`adminHello()` 方法体内的代码 "return '你好, [管理员]!'" 被执行。
8.  **[响应]**：前端收到 `200 OK` 和 "你好, [管理员]!"。