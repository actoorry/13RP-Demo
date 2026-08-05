package com.boyu.demo.module.purchase.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.purchase.entity.Apply;
import com.boyu.demo.module.purchase.service.ApplyService;
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

/**
 * 采购申请审批链接口：GET/POST/PUT /api/purchase/apply（批准→复核两段审批）。
 * <p>PUT 支持状态流转：status=APPROVED 批准；status=PENDING_REVIEW 进入待复核；status=REVIEWED 复核。
 */
@RestController
@RequestMapping("/api/purchase/apply")
public class ApplyController {

    private final ApplyService service;

    public ApplyController(ApplyService service) {
        this.service = service;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<Apply> w = new LambdaQueryWrapper<>();
        w.eq(status != null && !status.isBlank(), Apply::getStatus, status)
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
        if ("APPROVED".equalsIgnoreCase(status)) {
            service.approve(realId);
        } else if ("PENDING_REVIEW".equalsIgnoreCase(status)) {
            service.toReview(realId);
        } else if ("REVIEWED".equalsIgnoreCase(status)) {
            service.review(realId);
        } else {
            return Result.error("不支持的采购申请状态流转：" + status);
        }
        return Result.ok();
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
