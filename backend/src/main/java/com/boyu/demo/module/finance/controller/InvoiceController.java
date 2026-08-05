package com.boyu.demo.module.finance.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.finance.entity.Invoice;
import com.boyu.demo.module.finance.service.InvoiceService;
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
 * 发票管理接口：GET/POST/PUT/DELETE /api/finance/invoice。
 * <p>状态机：已新增 CREATED → 已审核 APPROVED → 已作废 VOID；支持反审核 APPROVED→CREATED。
 * PUT 流转触发：body 仅含状态字段（{@code {status:'APPROVED'/'CREATED'/'VOID'}}，前端流转按钮）
 * 或 {@code audit=approve/reject}；编辑表单提交完整实体走普通更新，状态以库中为准、置 null 防越权。
 * 非法迁移（VOID 再审核、非 APPROVED 反审核）由 Service 抛 IllegalStateException，Controller 捕获转 Result.error。
 */
@RestController
@RequestMapping("/api/finance/invoice")
public class InvoiceController {

    /** 状态流转允许出现的字段（其余字段出现视为普通编辑）。 */
    private static final Set<String> TRANSITION_KEYS = Set.of("id", "status", "audit");

    private final InvoiceService service;
    private final ObjectMapper objectMapper;

    public InvoiceController(InvoiceService service, ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(required = false) String invoiceType,
                                            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Invoice> w = new LambdaQueryWrapper<>();
        w.eq(status != null && !status.isBlank(), Invoice::getStatus, status)
                .eq(invoiceType != null && !invoiceType.isBlank(), Invoice::getInvoiceType, invoiceType)
                .and(keyword != null && !keyword.isBlank(),
                        wq -> wq.like(Invoice::getInvoiceNo, keyword)
                                .or().like(Invoice::getProductName, keyword))
                .orderByDesc(Invoice::getId);
        return Result.ok(PageQuery.toPageMap(service.page(new Page<>(query.getPage(), query.getSize()), w)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('finance:invoice:add')")
    public Result<Void> create(@RequestBody Invoice entity) {
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus("CREATED");
        }
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:invoice:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        // 路径 id 优先；body 未带 id 时用路径 id（兼容两种提交方式）
        Long realId = id != null ? id : longVal(body.get("id"));
        if (realId == null) {
            return Result.error("缺少发票 id");
        }
        String audit = str(body, "audit");
        String status = str(body, "status");
        boolean flowIntent = "approve".equalsIgnoreCase(audit) || "reject".equalsIgnoreCase(audit)
                || (status != null && !status.isBlank() && onlyTransitionFields(body));
        if (flowIntent) {
            try {
                if ("approve".equalsIgnoreCase(audit) || "APPROVED".equalsIgnoreCase(status)) {
                    service.approve(realId);
                } else if ("reject".equalsIgnoreCase(audit) || "CREATED".equalsIgnoreCase(status)) {
                    service.reject(realId);
                } else if ("VOID".equalsIgnoreCase(status)) {
                    service.voidInvoice(realId);
                } else {
                    return Result.error("不支持的发票状态流转：" + status);
                }
                return Result.ok();
            } catch (IllegalStateException e) {
                return Result.error(e.getMessage());
            }
        }
        // 普通编辑（金额/品名等）：状态只能走上方流转，置 null 防止越权
        Invoice entity = objectMapper.convertValue(body, Invoice.class);
        entity.setId(realId);
        entity.setStatus(null);
        service.updateById(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:invoice:delete')")
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
