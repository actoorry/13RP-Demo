package com.boyu.demo.module.org.service;

import com.boyu.demo.config.PermissionCache;
import com.boyu.demo.module.org.entity.SysPerson;
import com.boyu.demo.module.org.mapper.AuthMapper;
import com.boyu.demo.module.org.mapper.PersonMapper;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 我的权限服务：查看当前登录用户的菜单/权限码；刷新时清 Redis 缓存后重载。
 */
@Service
public class PermissionService {

    private final PersonMapper personMapper;
    private final AuthMapper authMapper;
    private final PermissionCache permissionCache;

    public PermissionService(PersonMapper personMapper, AuthMapper authMapper, PermissionCache permissionCache) {
        this.personMapper = personMapper;
        this.authMapper = authMapper;
        this.permissionCache = permissionCache;
    }

    /** 查看当前账号的菜单与权限码。 */
    public Map<String, Object> myPermissions(String account) {
        SysPerson p = personMapper.selectByAccount(account);
        if (p == null) {
            return Map.of();
        }
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("id", p.getId());
        user.put("account", p.getAccount());
        user.put("name", p.getName());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("user", user);
        data.put("menus", menus(p.getId()));
        data.put("permissions", authMapper.selectPermissionCodes(p.getId()));
        return data;
    }

    /** 刷新权限：删除 Redis 缓存并重新加载（权限刷新后越权拒绝立即生效）。 */
    public Map<String, Object> refresh(String account) {
        SysPerson p = personMapper.selectByAccount(account);
        if (p != null) {
            permissionCache.refresh(p.getId());
        }
        return myPermissions(account);
    }

    /** 菜单路径 /admin/base → base。 */
    private List<String> menus(Long personId) {
        return authMapper.selectMenuPaths(personId).stream()
                .map(path -> path == null ? "" : path.replace("/admin/", "").replace("/", ""))
                .filter(s -> !s.isBlank())
                .toList();
    }
}
