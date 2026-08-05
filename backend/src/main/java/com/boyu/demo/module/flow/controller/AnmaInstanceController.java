package com.boyu.demo.module.flow.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.flow.entity.AnmaInstance;
import com.boyu.demo.module.flow.service.AnmaInstanceService;
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
 * 安码流程实例接口：GET/POST/PUT/DELETE /api/flow/anma。
 * <p>PUT 审批流转：body 仅含流转字段（{@code {status:'DONE'/'REJECTED'}}）时调用 approve/reject；
 * 编辑表单提交完整实体走普通更新，状态以库中为准、置 null 防越权（状态只能走流转动作）。
 * 非法迁移由 Service 抛 IllegalStateException，Controller 捕获转 Result.error。
 */
@RestController
@RequestMapping("/api/flow/anma")
public class AnmaInstanceController {

    /** 审批流转允许出现的字段（其余字段出现视为普通编辑）。 */
    private static final Set<String> TRANSITION_KEYS = Set.of("id", "status");

    private final AnmaInstanceService service;
    private final ObjectMapper objectMapper;

    public AnmaInstanceController(AnmaInstanceService service, ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String flowType,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<AnmaInstance> w = new LambdaQueryWrapper<>();
        w.eq(flowType != null && !flowType.isBlank(), AnmaInstance::getFlowType, flowType)
                .eq(status != null && !status.isBlank(), AnmaInstance::getStatus, status)
                .and(keyword != null && !keyword.isBlank(),
                        wq -> wq.like(AnmaInstance::getFlowNo, keyword)
                                .or().like(AnmaInstance::getTitle, keyword)
                                .or().like(AnmaInstance::getFlowType, keyword))
                .orderByDesc(AnmaInstance::getId);
        return Result.ok(PageQuery.toPageMap(service.pageWithNames(new Page<>(query.getPage(), query.getSize()), w)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('flow:anma:add')")
    public Result<Void> create(@RequestBody AnmaInstance entity) {
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus("RUNNING");
        }
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('flow:anma:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        // 路径 id 优先；body 未带 id 时用路径 id（兼容两种提交方式）
        Long realId = id != null ? id : longVal(body.get("id"));
        if (realId == null) {
            return Result.error("缺少流程实例 id");
        }
        String status = str(body, "status");
        boolean flowIntent = ("DONE".equalsIgnoreCase(status) || "REJECTED".equalsIgnoreCase(status))
                && onlyTransitionFields(body);
        if (flowIntent) {
            try {
                if ("DONE".equalsIgnoreCase(status)) {
                    service.approve(realId);
                } else {
                    service.reject(realId);
                }
                return Result.ok();
            } catch (IllegalStateException e) {
                return Result.error(e.getMessage());
            }
        }
        // 普通编辑（标题/金额等）：状态只能走上方流转动作，置 null 防止越权流转
        AnmaInstance entity = objectMapper.convertValue(body, AnmaInstance.class);
        entity.setId(realId);
        entity.setStatus(null);
        service.updateById(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('flow:anma:delete')")
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
