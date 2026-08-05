package com.boyu.demo.controller;

import com.boyu.demo.common.Result;
import com.boyu.demo.common.SecurityUtils;
import com.boyu.demo.module.org.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 登录认证接口：POST /api/auth/login → {token, user, menus, permissions}。
 * <p>匿名放行（SecurityConfig 已 permitAll）。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest req) {
        return authService.login(req.account(), req.password());
    }

    /** 当前登录用户（需 JWT；无 token 由 SecurityConfig 返回 401）。 */
    @GetMapping("/me")
    public Result<Map<String, Object>> me() {
        return Result.ok(authService.me(SecurityUtils.currentAccount()));
    }

    /** 登录请求体（契约 §3.2）：{ "account": "admin", "password": "123456" }。 */
    public record LoginRequest(String account, String password) {
    }
}
