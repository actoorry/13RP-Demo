package com.boyu.demo.module.org.controller;

import com.boyu.demo.common.Result;
import com.boyu.demo.common.SecurityUtils;
import com.boyu.demo.module.org.service.PermissionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 我的权限接口（契约 §3.4）：GET /api/org/permission 查看；PUT /api/org/permission 刷新。
 */
@RestController
@RequestMapping("/api/org/permission")
public class PermissionController {

    private final PermissionService service;

    public PermissionController(PermissionService service) {
        this.service = service;
    }

    /** 查看我的菜单/权限。 */
    @GetMapping
    public Result<Map<String, Object>> myPermissions() {
        return Result.ok(service.myPermissions(SecurityUtils.currentAccount()));
    }

    /** 刷新权限（清 Redis 缓存并重新加载）。 */
    @PutMapping
    @PreAuthorize("hasAuthority('org:permission:update')")
    public Result<Map<String, Object>> refresh() {
        return Result.ok(service.refresh(SecurityUtils.currentAccount()));
    }
}
