package com.boyu.demo.module.flow.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.flow.entity.X5Instance;
import com.boyu.demo.module.flow.service.X5InstanceService;
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
 * X5 流程实例接口：GET/POST/PUT/DELETE /api/flow/x5。
 * <p>POST 按金额自动生成审批任务（500 元分级审批）；PUT 审批流转：body 仅含流转字段（{@code {status:'DONE'/'REJECTED'}}）
 * 时调用 approve/reject；编辑表单提交完整实体走普通更新，状态以库中为准、置 null 防越权（状态只能走流转动作）。
 * 非法迁移由 Service 抛 IllegalStateException，Controller 捕获转 Result.error。
 */
@RestController
@RequestMapping("/api/flow/x5")
public class X5InstanceController {

    /** 审批流转允许出现的字段（其余字段出现视为普通编辑）。 */
    private static final Set<String> TRANSITION_KEYS = Set.of("id", "status");

    private final X5InstanceService service;
    private final ObjectMapper objectMapper;

    public X5InstanceController(X5InstanceService service, ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String flowType,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(required = false) String applicant,
                                            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<X5Instance> w = new LambdaQueryWrapper<>();
        w.eq(flowType != null && !flowType.isBlank(), X5Instance::getFlowType, flowType)
                .eq(status != null && !status.isBlank(), X5Instance::getStatus, status)
                .eq(applicant != null && !applicant.isBlank(), X5Instance::getApplicant, applicant)
                .and(keyword != null && !keyword.isBlank(),
                        wq -> wq.like(X5Instance::getFlowNo, keyword)
                                .or().like(X5Instance::getTitle, keyword)
                                .or().like(X5Instance::getFlowType, keyword)
                                .or().like(X5Instance::getApplicant, keyword))
                .orderByDesc(X5Instance::getId);
        return Result.ok(PageQuery.toPageMap(service.page(new Page<>(query.getPage(), query.getSize()), w)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('flow:x5:add')")
    public Result<Void> create(@RequestBody X5Instance entity) {
        service.createInstance(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('flow:x5:update')")
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
        X5Instance entity = objectMapper.convertValue(body, X5Instance.class);
        entity.setId(realId);
        entity.setStatus(null);
        service.updateById(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('flow:x5:delete')")
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
