package com.gb.test.springsecuritydemo.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    // 1. 定义一个绝对保密的密钥 (Secret Key)
    // !! 警告：在生产环境中，绝不能硬编码！必须从配置文件 (application.properties) 读取
    // 比如： @Value("${jwt.secret}") private String secretString;
    // 这里的密钥是一个示例，它至少需要256位（32字节）长
    private final String SECRET_STRING = "MySuperSecureSecretKeyForDemoApp123456";

    // 2. 将字符串密钥转换为 SecretKey 对象
    private final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes());

    // 3. 定义令牌的过期时间（例如：1小时）
    private final long EXPIRATION_TIME = 1000 * 60 * 60; // 1小时 (毫秒)

    /**
     * A. 从令牌中提取用户名 (Subject)
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * B. 从令牌中提取过期时间
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * C. 检查令牌是否已过期
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * D. 核心：生成令牌
     * (这个方法会在用户登录成功时被调用)
     * @param username 用户的身份标识
     * @return JWT 字符串
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        // 你可以在这里添加自定义的 claims, 比如用户的角色
        // claims.put("roles", userDetails.getAuthorities());

        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date(System.currentTimeMillis());
        Date expirationDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .claims(claims)       // 设置自定义 claims
                .subject(subject)     // 设置主题（用户名）
                .issuedAt(now)        // 设置签发时间
                .expiration(expirationDate) // 设置过期时间
                .signWith(SECRET_KEY, Jwts.SIG.HS256) // 使用 HS256 算法和密钥签名
                .compact();
    }

    /**
     * E. 核心：验证令牌是否有效
     * (这个方法会在每次收到请求时被调用)
     * @param token 令牌
     * @param username 从 UserDetails 中获取的用户名
     * @return 是否有效
     */
    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        // 检查用户名是否一致，并且令牌未过期
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    // --- 辅助方法 ---

    /**
     * F. 辅助：从令牌中提取单个 Claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * G. 辅助：解析整个令牌 (JWS - 已签名的 JWT)
     * (这是验证签名和解析 Payload 的地方)
     */
    private Claims extractAllClaims(String token) {
        // Jwts.parser() 会自动使用我们提供的密钥来验证签名的合法性
        // 如果签名不匹配、令牌过期、或格式错误，它会抛出异常
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}