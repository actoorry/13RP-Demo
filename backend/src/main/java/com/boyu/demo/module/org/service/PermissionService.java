package com.boyu.demo.module.org.service;

import com.boyu.demo.config.PermissionCache;
import com.boyu.demo.module.org.entity.SysPerson;
import com.boyu.demo.module.org.mapper.AuthMapper;
import com.boyu.demo.module.org.mapper.PersonMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 我的权限服务：查看当前登录用户的身份/角色/菜单/权限码；刷新时清 Redis 缓存后重载。
 * <p>权限来源 = 角色（sys_person_role → sys_role_menu → sys_menu）。
 */
@Service
public class PermissionService {

    /** 模块前缀 → 中文名（与前端导航/路由一致）。 */
    private static final Map<String, String> MODULE_NAMES = Map.ofEntries(
            Map.entry("base", "基础数据与系统管理"),
            Map.entry("org", "组织与权限"),
            Map.entry("purchase", "采购"),
            Map.entry("sale", "销售"),
            Map.entry("inventory", "库存"),
            Map.entry("finance", "财务"),
            Map.entry("crm", "CRM"),
            Map.entry("flow", "流程引擎"),
            Map.entry("todo", "待办事宜")
    );

    private final PersonMapper personMapper;
    private final AuthMapper authMapper;
    private final PermissionCache permissionCache;

    public PermissionService(PersonMapper personMapper, AuthMapper authMapper, PermissionCache permissionCache) {
        this.personMapper = personMapper;
        this.authMapper = authMapper;
        this.permissionCache = permissionCache;
    }

    /** 查看当前账号的身份/角色/菜单与权限（按模块分组）。 */
    public Map<String, Object> myPermissions(String account) {
        SysPerson p = personMapper.selectByAccount(account);
        if (p == null) {
            return Map.of();
        }
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("id", p.getId());
        user.put("account", p.getAccount());
        user.put("name", p.getName());
        user.put("dept", p.getDept());
        user.put("position", p.getPosition());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("user", user);
        data.put("roles", authMapper.selectRoleNames(p.getId()));
        data.put("menus", menus(p.getId()));
        data.put("permissions", authMapper.selectPermissionCodes(p.getId()));
        data.put("permissionGroups", permissionGroups(p.getId()));
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

    /** 权限明细按模块分组（模块中文名 + 权限项 [{code, name}]）。 */
    private List<Map<String, Object>> permissionGroups(Long personId) {
        List<Map<String, Object>> details = authMapper.selectPermissionDetails(personId);
        Map<String, List<Map<String, Object>>> grouped = new LinkedHashMap<>();
        for (Map<String, Object> d : details) {
            String mod = d.get("module") == null ? "other" : String.valueOf(d.get("module"));
            grouped.computeIfAbsent(mod, k -> new ArrayList<>()).add(d);
        }
        List<Map<String, Object>> groups = new ArrayList<>();
        grouped.forEach((mod, items) -> {
            Map<String, Object> g = new LinkedHashMap<>();
            g.put("module", mod);
            g.put("moduleName", MODULE_NAMES.getOrDefault(mod, mod));
            g.put("items", items);
            groups.add(g);
        });
        return groups;
    }
}
