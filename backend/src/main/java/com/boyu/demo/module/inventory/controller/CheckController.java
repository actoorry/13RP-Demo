package com.boyu.demo.module.inventory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.inventory.entity.InventoryCheck;
import com.boyu.demo.module.inventory.service.InventoryCheckService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

/**
 * 盘点接口：GET/POST/PUT/DELETE /api/inventory/check。
 * <p>PUT 审批流转：status=APPROVED（或 action=approve）批准；status=CHECKED（或 action=check）保管员审核（与入库同模式）。
 * 流转仅当 body 只含流转字段（{@code id/status/action}）时触发；编辑表单提交完整实体走普通更新，
 * 状态以库中为准、置 null 防越权。非法迁移由 Service 抛 IllegalStateException，此处转 Result.error。
 */
@RestController
@RequestMapping("/api/inventory/check")
public class CheckController {

    /** 盘点流转允许出现的字段（其余字段出现视为普通编辑）。 */
    private static final Set<String> TRANSITION_KEYS = Set.of("id", "status", "action");

    private final InventoryCheckService service;
    private final ObjectMapper objectMapper;

    public CheckController(InventoryCheckService service, ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<InventoryCheck> w = new LambdaQueryWrapper<>();
        w.and(keyword != null && !keyword.isBlank(),
                        wq -> wq.like(InventoryCheck::getCheckNo, keyword)
                                .or().like(InventoryCheck::getBatchNo, keyword))
                .orderByDesc(InventoryCheck::getId);
        return Result.ok(PageQuery.toPageMap(service.page(new Page<>(query.getPage(), query.getSize()), w)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('inventory:check:add')")
    public Result<Void> create(@RequestBody InventoryCheck entity) {
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus("CREATED");
        }
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:check:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long realId = id != null ? id : longVal(body.get("id"));
        if (realId == null) {
            return Result.error("缺少盘点单 id");
        }
        String status = str(body, "status");
        String action = str(body, "action");
        boolean approveIntent = "approve".equalsIgnoreCase(action)
                || ("APPROVED".equalsIgnoreCase(status) && onlyTransitionFields(body));
        boolean checkIntent = "check".equalsIgnoreCase(action)
                || ("CHECKED".equalsIgnoreCase(status) && onlyTransitionFields(body));
        if (approveIntent) {
            try {
                service.approve(realId);
                return Result.ok();
            } catch (IllegalStateException e) {
                return Result.error(e.getMessage());
            }
        }
        if (checkIntent) {
            try {
                service.check(realId);
                return Result.ok();
            } catch (IllegalStateException e) {
                return Result.error(e.getMessage());
            }
        }
        // 普通编辑：状态只能走流转动作，置 null 防止越权流转
        InventoryCheck entity = objectMapper.convertValue(body, InventoryCheck.class);
        entity.setId(realId);
        entity.setStatus(null);
        service.updateById(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:check:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }

    private static boolean onlyTransitionFields(Map<String, Object> body) {
        for (String k : body.keySet()) {
            if (TRANSITION_KEYS.contains(k)) {
                continue;
            }
            return false;
        }
        return true;
    }

    private static String str(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return v == null ? null : String.valueOf(v);
    }

    private static Long longVal(Object v) {
        if (v instanceof Number n) {
            return n.longValue();
        }
        if (v instanceof String s) {
            try {
                return Long.parseLong(s.trim());
            } catch (NumberFormatException ignore) {
                return null;
            }
        }
        return null;
    }
}
