package com.boyu.demo.module.sale.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.sale.entity.SaleInvoiceApply;
import com.boyu.demo.module.sale.service.SaleInvoiceApplyService;
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
 * 开票申请接口：GET/POST/PUT/DELETE /api/sale/invoice-apply。
 * <p>PUT 状态流转：status=PENDING 申请→待开；status=ISSUED 待开→已开。
 * 流转仅当 body 只含流转字段（{@code id/status/action}）时触发；编辑表单提交完整实体走普通更新，
 * 状态以库中为准、置 null 防越权。非法迁移由 Service 抛 IllegalStateException，此处转 Result.error。
 */
@RestController
@RequestMapping("/api/sale/invoice-apply")
public class SaleInvoiceApplyController {

    /** 开票流转允许出现的字段（其余字段出现视为普通编辑）。 */
    private static final Set<String> TRANSITION_KEYS = Set.of("id", "status", "action");

    private final SaleInvoiceApplyService service;
    private final ObjectMapper objectMapper;

    public SaleInvoiceApplyController(SaleInvoiceApplyService service, ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<SaleInvoiceApply> w = new LambdaQueryWrapper<>();
        w.eq(status != null && !status.isBlank(), SaleInvoiceApply::getStatus, status)
                .and(keyword != null && !keyword.isBlank(),
                        wq -> wq.like(SaleInvoiceApply::getApplyNo, keyword)
                                .or().like(SaleInvoiceApply::getInvoiceNo, keyword))
                .orderByDesc(SaleInvoiceApply::getId);
        return Result.ok(PageQuery.toPageMap(service.page(new Page<>(query.getPage(), query.getSize()), w)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sale:invoice-apply:add')")
    public Result<Void> create(@RequestBody SaleInvoiceApply entity) {
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus("APPLIED");
        }
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sale:invoice-apply:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long realId = id != null ? id : longVal(body.get("id"));
        if (realId == null) {
            return Result.error("缺少开票申请 id");
        }
        String status = str(body, "status");
        String action = str(body, "action");
        boolean pendingIntent = "pending".equalsIgnoreCase(action)
                || ("PENDING".equalsIgnoreCase(status) && onlyTransitionFields(body));
        boolean issueIntent = "issue".equalsIgnoreCase(action)
                || ("ISSUED".equalsIgnoreCase(status) && onlyTransitionFields(body));
        if (pendingIntent) {
            try {
                service.toPending(realId);
                return Result.ok();
            } catch (IllegalStateException e) {
                return Result.error(e.getMessage());
            }
        }
        if (issueIntent) {
            try {
                service.issue(realId);
                return Result.ok();
            } catch (IllegalStateException e) {
                return Result.error(e.getMessage());
            }
        }
        // 普通编辑（改申请单号/发票号等）：状态只能走流转动作，置 null 防止越权流转
        SaleInvoiceApply entity = objectMapper.convertValue(body, SaleInvoiceApply.class);
        entity.setId(realId);
        entity.setStatus(null);
        service.updateById(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sale:invoice-apply:delete')")
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
