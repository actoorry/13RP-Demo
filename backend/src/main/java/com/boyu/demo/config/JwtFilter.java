package com.boyu.demo.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 解析过滤器：解析 {@code Authorization: Bearer <token>} → 校验签名 →
 * 从 {@link PermissionCache} 加载权限码到 SecurityContext（供 @PreAuthorize 使用）。
 * <p>放行路径（/api/auth/login、/api/demo/**、/ws/**）在 SecurityConfig 中 permitAll，
 * 此过滤器解析失败不阻断（由 Security 决定 401）。
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final PermissionCache permissionCache;

    public JwtFilter(JwtUtil jwtUtil, PermissionCache permissionCache) {
        this.jwtUtil = jwtUtil;
        this.permissionCache = permissionCache;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());
            try {
                Claims claims = jwtUtil.parseToken(token);
                Long personId = jwtUtil.getPersonId(claims);
                String account = jwtUtil.getAccount(claims);
                List<String> perms = personId != null ? permissionCache.getPermissions(personId) : List.of();
                List<GrantedAuthority> authorities = perms.stream()
                        .<GrantedAuthority>map(SimpleGrantedAuthority::new)
                        .toList();
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(account, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                // 无效/过期 token：清除上下文，由 Security 返回 401
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
