package com.boyu.demo.module.purchase.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.purchase.entity.Apply;
import com.boyu.demo.module.purchase.service.ApplyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.access.prepost.PreAuthorize;
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
 * 采购申请审批链接口：GET/POST/PUT /api/purchase/apply（批准→复核两段审批）。
 * <p>PUT 流转触发：body 仅含流转字段（{@code {id?, status}} 且 status 为 APPROVED/PENDING_REVIEW/REVIEWED）；
 * 编辑表单提交完整实体走普通更新，状态以库中为准、置 null 防越权。
 * 非法迁移由 Service 抛 IllegalStateException，此处捕获转 Result.error。
 */
@RestController
@RequestMapping("/api/purchase/apply")
public class ApplyController {

    /** 审批流转允许出现的字段（其余字段出现视为普通编辑）。 */
    private static final Set<String> TRANSITION_KEYS = Set.of("id", "status");

    private final ApplyService service;
    private final ObjectMapper objectMapper;

    public ApplyController(ApplyService service, ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Apply> w = new LambdaQueryWrapper<>();
        w.eq(status != null && !status.isBlank(), Apply::getStatus, status)
                .and(keyword != null && !keyword.isBlank(),
                        wq -> wq.like(Apply::getApplyNo, keyword)
                                .or().like(Apply::getApplicant, keyword))
                .orderByDesc(Apply::getId);
        return Result.ok(PageQuery.toPageMap(service.page(new Page<>(query.getPage(), query.getSize()), w)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('purchase:apply:add')")
    public Result<Void> create(@RequestBody Apply entity) {
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus("PENDING_APPROVE");
        }
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('purchase:apply:approve', 'purchase:apply:review')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        // 路径 id 优先；body 未带 id 时用路径 id（兼容两种提交方式）
        Long realId = id != null ? id : longVal(body.get("id"));
        if (realId == null) {
            return Result.error("缺少采购申请单 id");
        }
        String status = str(body, "status");
        boolean approveIntent = "APPROVED".equalsIgnoreCase(status) && onlyTransitionFields(body);
        boolean toReviewIntent = "PENDING_REVIEW".equalsIgnoreCase(status) && onlyTransitionFields(body);
        boolean reviewIntent = "REVIEWED".equalsIgnoreCase(status) && onlyTransitionFields(body);
        try {
            if (approveIntent) {
                service.approve(realId);
                return Result.ok();
            }
            if (toReviewIntent) {
                service.toReview(realId);
                return Result.ok();
            }
            if (reviewIntent) {
                service.review(realId);
                return Result.ok();
            }
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
        // 普通编辑（编辑申请单基础字段）：审批状态只能走上方流转动作，置 null 防止越权流转
        Apply entity = objectMapper.convertValue(body, Apply.class);
        entity.setId(realId);
        entity.setStatus(null);
        service.updateById(entity);
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
