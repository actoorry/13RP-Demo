package com.boyu.demo.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前登录上下文工具（JwtFilter 注入 principal = account）。
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /** 当前登录账号；未登录返回 null。 */
    public static String currentAccount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof String s) {
            return s;
        }
        return null;
    }
}
