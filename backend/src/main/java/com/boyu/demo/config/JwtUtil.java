package com.boyu.demo.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * JWT 工具类（jjwt 0.12 API）。
 * <p>⚠️ 已知坑：必须用 {@code Keys.hmacShaKeyFor(secret.getBytes())} + {@code Jwts.builder().signWith(key)}，
 * 不能用 0.9 旧 API（setSubject / HS256 字符串签名会编译失败或运行时异常）。
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expireMillis;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expire-hours:12}") long expireHours) {
        // secret 至少 32 字符（HS256 需要 ≥ 256 bit）
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireMillis = expireHours * 3600_000L;
    }

    /** 生成 token：subject=account，claims 携带 personId 与 roleIds。 */
    public String generateToken(Long personId, String account, List<Long> roleIds) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expireMillis);
        return Jwts.builder()
                .subject(account)
                .claim("personId", personId)
                .claim("roles", roleIds)
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    /** 解析 token；签名/过期不合法抛出 JwtException。 */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 从 claims 提取人员 id。 */
    public Long getPersonId(Claims claims) {
        Object v = claims.get("personId");
        if (v instanceof Number n) {
            return n.longValue();
        }
        return null;
    }

    /** 从 claims 提取登录账号。 */
    public String getAccount(Claims claims) {
        return claims.getSubject();
    }
}
