package com.seckill.common;        // 工具类放在 common（通用）包下

import io.jsonwebtoken.*;                    // JJWT 库的所有类（JwtBuilder, JwtParser, Claims 等）
import io.jsonwebtoken.security.Keys;         // 用于生成 HMAC-SHA256 密钥
import org.springframework.beans.factory.annotation.Value;    // 从 application.yml 中读取配置值
import org.springframework.stereotype.Component;              // 标记为 Spring 管理的组件

import javax.crypto.SecretKey;               // Java 加密包：密钥对象
import java.nio.charset.StandardCharsets;     // 字符编码常量（UTF_8）
import java.util.Date;                        // 日期类（用于设置 Token 的签发时间和过期时间）

@Component                   // 标记为 Spring 组件：启动时自动创建 JwtUtils 的单例对象，供其他类注入使用
public class JwtUtils {

    private final SecretKey key;     // HMAC-SHA256 密钥（从 jwt.secret 配置生成），用于签名和验证 Token
    private final long expiration;   // Token 过期时间（毫秒），从 jwt.expiration 配置读取

    public JwtUtils(@Value("${jwt.secret}") String secret,      // @Value：从 application.yml 读取配置
                    @Value("${jwt.expiration}") long expiration) { // ${jwt.secret} → "seckill-platform-jwt-..."
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); // 用 secret 字符串生成密钥
        this.expiration = expiration;      // 604800000 毫秒 = 7 天
    }

    /** 生成 Token */
    public String generateToken(Long userId) {                   // 传入用户 ID，生成 JWT 字符串
        Date now = new Date();                                   // 当前时间
        Date expiryDate = new Date(now.getTime() + expiration);  // 过期时间 = 现在 + 7 天
        return Jwts.builder()                    // JWT 构建器（建造者模式）
                .subject(String.valueOf(userId)) // sub 字段：存用户 ID（JWT 标准字段之一）
                .issuedAt(now)                   // iat 字段：签发时间
                .expiration(expiryDate)          // exp 字段：过期时间
                .signWith(key)                   // 用 HMAC-SHA256 签名，防止 Token 被篡改
                .compact();                      // 生成最终的 JWT 字符串：头部.载荷.签名 三部分用 . 分隔
    }

    /** 从 Token 中提取 user_id */
    public Long getUserIdFromToken(String token) {  // 传入 Token 字符串，解析出用户 ID
        Claims claims = parseToken(token);          // Claims 就是 JWT 的载荷（payload），以 Map 形式存数据
        return Long.valueOf(claims.getSubject());    // getSubject() 取 sub 字段 → 转为 Long 类型
    }

    /** 验证 Token 是否有效 */
    public boolean validateToken(String token) {    // 验证 Token 是否合法且未过期
        try {
            parseToken(token);                      // 尝试解析 Token
            return true;                            // 解析成功 → Token 有效
        } catch (JwtException | IllegalArgumentException e) { // JwtException：签名不对/过期/格式错误
            return false;                           // 解析失败 → Token 无效
        }
    }

    private Claims parseToken(String token) {        // 私有方法：解析并验证 JWT
        return Jwts.parser()                         // 创建 JWT 解析器
                .verifyWith(key)                      // 设置验证密钥（如果签名不匹配会抛异常）
                .build()                              // 构建解析器对象
                .parseSignedClaims(token)             // 解析 Token 并验证签名
                .getPayload();                        // 获取载荷（Claims），即存用户数据的部分
    }
}
