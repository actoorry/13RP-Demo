package com.boyu.demo.module.org.service;

import com.boyu.demo.common.Result;
import com.boyu.demo.config.JwtUtil;
import com.boyu.demo.config.PermissionCache;
import com.boyu.demo.module.org.entity.SysPerson;
import com.boyu.demo.module.org.mapper.AuthMapper;
import com.boyu.demo.module.org.mapper.PersonMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 登录认证服务：账号密码校验 → 生成 JWT → 返回用户/菜单/权限。
 * <p>返回结构契约 §3.2：data = { token, user: {id, account, name}, menus, permissions }。
 */
@Service
public class AuthService {

    private final PersonMapper personMapper;
    private final AuthMapper authMapper;
    private final JwtUtil jwtUtil;
    private final PermissionCache permissionCache;
    private final PasswordEncoder passwordEncoder;

    public AuthService(PersonMapper personMapper, AuthMapper authMapper, JwtUtil jwtUtil,
                       PermissionCache permissionCache, PasswordEncoder passwordEncoder) {
        this.personMapper = personMapper;
        this.authMapper = authMapper;
        this.jwtUtil = jwtUtil;
        this.permissionCache = permissionCache;
        this.passwordEncoder = passwordEncoder;
    }

    /** 登录：账号/密码校验失败返回 code=1；成功返回 token + 用户 + 菜单 + 权限。 */
    public Result<Map<String, Object>> login(String account, String password) {
        if (account == null || account.isBlank() || password == null || password.isBlank()) {
            return Result.error("账号或密码不能为空");
        }
        SysPerson person = personMapper.selectByAccount(account.trim());
        if (person == null || !passwordEncoder.matches(password, person.getPassword())) {
            return Result.error("账号或密码错误");
        }
        Long personId = person.getId();
        List<Long> roleIds = authMapper.selectRoleIds(personId);
        String token = jwtUtil.generateToken(personId, person.getAccount(), roleIds);

        // 菜单：/admin/base → base（与契约 §3.2 示例 ["base","org","purchase"] 一致）
        List<String> menus = authMapper.selectMenuPaths(personId).stream()
                .map(p -> p == null ? "" : p.replace("/admin/", "").replace("/", ""))
                .filter(p -> !p.isBlank())
                .toList();
        List<String> permissions = authMapper.selectPermissionCodes(personId);
        permissionCache.cachePermissions(personId, permissions);

        Map<String, Object> user = new LinkedHashMap<>();
        user.put("id", personId);
        user.put("account", person.getAccount());
        user.put("name", person.getName());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", token);
        data.put("user", user);
        data.put("menus", menus);
        data.put("permissions", permissions);
        return Result.ok(data);
    }

    /** 当前登录用户信息（/api/auth/me，需 JWT）：data = { user: {id, account, name} }。 */
    public Map<String, Object> me(String account) {
        Map<String, Object> data = new LinkedHashMap<>();
        if (account == null || account.isBlank()) {
            data.put("user", null);
            return data;
        }
        SysPerson person = personMapper.selectByAccount(account.trim());
        if (person == null) {
            data.put("user", null);
            return data;
        }
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("id", person.getId());
        user.put("account", person.getAccount());
        user.put("name", person.getName());
        data.put("user", user);
        return data;
    }
}
