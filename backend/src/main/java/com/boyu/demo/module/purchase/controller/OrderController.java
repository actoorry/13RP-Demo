package com.boyu.demo.module.purchase.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.purchase.entity.PurchaseOrder;
import com.boyu.demo.module.purchase.service.OrderService;
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
 * 待审批订单 + 结算分流 + 关闭接口：GET/POST/PUT /api/purchase/order。
 * <p>PUT 流转触发：body 仅含流转字段（{@code {status:'APPROVED'}} 审批 / {@code {status:'PAID'}|{action:'PAY'}} 付款 /
 * {@code {status:'CLOSED'}} 关闭订单）。审批按结算方式分流：现款后货→待付款 WAIT_PAY / 先货后款→待入库 WAIT_INBOUND；
 * 关闭为终态，关闭后不可审批/付款。编辑表单提交完整实体走普通更新，状态以库中为准、置 null 防越权。
 * 非法迁移（非待审批审批、非待付款付款、非待审批/已审批关闭）由 Service 抛 IllegalStateException，此处捕获转 Result.error。
 */
@RestController
@RequestMapping("/api/purchase/order")
public class OrderController {

    /** 订单流转允许出现的字段（其余字段出现视为普通编辑）。 */
    private static final Set<String> TRANSITION_KEYS = Set.of("id", "status", "action", "pay");

    private final OrderService service;
    private final ObjectMapper objectMapper;

    public OrderController(OrderService service, ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(required = false) String settlementStatus) {
        LambdaQueryWrapper<PurchaseOrder> w = new LambdaQueryWrapper<>();
        w.eq(status != null && !status.isBlank(), PurchaseOrder::getStatus, status)
                .eq(settlementStatus != null && !settlementStatus.isBlank(), PurchaseOrder::getSettlementStatus, settlementStatus)
                .orderByDesc(PurchaseOrder::getId);
        return Result.ok(PageQuery.toPageMap(service.page(new Page<>(query.getPage(), query.getSize()), w)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('purchase:order:add')")
    public Result<Void> create(@RequestBody PurchaseOrder entity) {
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus("PENDING_APPROVE");
        }
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('purchase:order:approve', 'purchase:order:pay', 'purchase:order:close')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        // 路径 id 优先；body 未带 id 时用路径 id（兼容两种提交方式）
        Long realId = id != null ? id : longVal(body.get("id"));
        if (realId == null) {
            return Result.error("缺少订单 id");
        }
        String status = str(body, "status");
        String action = str(body, "action");
        boolean approveIntent = "APPROVED".equalsIgnoreCase(status) && onlyTransitionFields(body);
        boolean payIntent = ("PAID".equalsIgnoreCase(status) || "PAY".equalsIgnoreCase(action)
                || Boolean.TRUE.equals(body.get("pay"))) && onlyTransitionFields(body);
        boolean closeIntent = "CLOSED".equalsIgnoreCase(status) && onlyTransitionFields(body);
        try {
            if (approveIntent) {
                service.approve(realId);
                return Result.ok();
            }
            if (payIntent) {
                service.pay(realId);
                return Result.ok();
            }
            if (closeIntent) {
                service.close(realId);
                return Result.ok();
            }
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
        // 普通编辑（制单人/金额/供应商等）：状态只能走上方流转动作，置 null 防止越权流转
        PurchaseOrder entity = objectMapper.convertValue(body, PurchaseOrder.class);
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
