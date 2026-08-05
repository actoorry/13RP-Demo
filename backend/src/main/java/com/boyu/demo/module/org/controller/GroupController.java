package com.boyu.demo.module.org.controller;

import com.boyu.demo.common.Result;
import com.boyu.demo.module.org.entity.OrgGroup;
import com.boyu.demo.module.org.service.GroupService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 组管理接口（契约 §3.4）：
 * <ul>
 *   <li>GET  /api/org/group — 组列表（含组内客户数）</li>
 *   <li>GET  /api/org/group/{id}/customers — 组内客户 vs 我的客户</li>
 *   <li>POST /api/org/group — 新建组</li>
 *   <li>PUT  /api/org/group — 更新组</li>
 *   <li>POST /api/org/group/transfer — 划拨到公司/划拨到组/批量迁移主要负责人</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/org/group")
public class GroupController {

    private final GroupService service;

    public GroupController(GroupService service) {
        this.service = service;
    }

    @GetMapping
    public Result<Map<String, Object>> list() {
        return Result.ok(service.groupList());
    }

    /** 组详情：组内客户（组级共享）+ 我的客户（个人负责）。 */
    @GetMapping("/{id}/customers")
    public Result<Map<String, Object>> customers(@PathVariable Long id) {
        return Result.ok(service.detail(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('org:group:add')")
    public Result<Void> create(@RequestBody OrgGroup entity) {
        service.groupMapper().insert(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('org:group:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody OrgGroup entity) {
        entity.setId(id);
        service.groupMapper().updateById(entity);
        return Result.ok();
    }

    /** 划拨到公司 / 划拨到组 / 批量迁移主要负责人。 */
    @PostMapping("/transfer")
    @PreAuthorize("hasAuthority('org:group:transfer')")
    public Result<Void> transfer(@RequestBody Map<String, Object> body) {
        return service.transfer(body);
    }
}
