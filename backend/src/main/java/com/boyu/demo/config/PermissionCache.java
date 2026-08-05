package com.boyu.demo.config;

import com.boyu.demo.module.org.mapper.AuthMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * 权限缓存：Redis 缓存「人员 id → 权限码列表」，Redis 不可用时降级查库（try-catch，⚠️ 已知坑）。
 * <p>key 约定：{@code auth:perm:{personId}}，value 为逗号分隔的权限码串。
 */
@Component
public class PermissionCache {

    private static final String PREFIX = "auth:perm:";

    private final StringRedisTemplate redis;
    private final AuthMapper authMapper;
    private final long expireHours;

    public PermissionCache(StringRedisTemplate redis, AuthMapper authMapper,
                           @Value("${jwt.expire-hours:12}") long expireHours) {
        this.redis = redis;
        this.authMapper = authMapper;
        this.expireHours = expireHours;
    }

    /** 取人员权限码：优先 Redis，未命中或异常则查库并回填。 */
    public List<String> getPermissions(Long personId) {
        String key = PREFIX + personId;
        try {
            String cached = redis.opsForValue().get(key);
            if (cached != null && !cached.isBlank()) {
                return Arrays.stream(cached.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
            }
        } catch (Exception ignore) {
            // Redis 不可用 → 降级查库
        }
        List<String> perms = authMapper.selectPermissionCodes(personId);
        try {
            redis.opsForValue().set(key, String.join(",", perms), Duration.ofHours(expireHours));
        } catch (Exception ignore) {
            // 降级：不缓存也不影响本次请求
        }
        return perms;
    }

    /** 登录成功后写入权限缓存。 */
    public void cachePermissions(Long personId, List<String> permissions) {
        try {
            redis.opsForValue().set(PREFIX + personId, String.join(",", permissions),
                    Duration.ofHours(expireHours));
        } catch (Exception ignore) {
            // 降级：无 Redis 也能运行
        }
    }

    /** 权限刷新：删除缓存，下次请求重新加载。 */
    public void refresh(Long personId) {
        try {
            redis.delete(PREFIX + personId);
        } catch (Exception ignore) {
            // 降级：无 Redis 也能运行
        }
    }
}
