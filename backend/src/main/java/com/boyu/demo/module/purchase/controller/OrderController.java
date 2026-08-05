package com.boyu.demo.module.purchase.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.purchase.entity.PurchaseOrder;
import com.boyu.demo.module.purchase.service.OrderService;
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
 * 待审批订单 + 结算分流接口：GET/POST/PUT /api/purchase/order。
 * <p>PUT 支持：status=APPROVED 审批（按结算方式分流：现款后货→待付款 WAIT_PAY / 先货后款→待入库 WAIT_INBOUND）；
 * action=PAY 或 status=PAID 现款后货付款 → 待入库 WAIT_INBOUND。
 */
@RestController
@RequestMapping("/api/purchase/order")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
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
    @PreAuthorize("hasAnyAuthority('purchase:order:approve', 'purchase:order:pay')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        // 路径 id 优先；body 未带 id 时用路径 id（兼容两种提交方式）
        Long realId = id != null ? id : longVal(body.get("id"));
        if (realId == null) {
            return Result.error("缺少订单 id");
        }
        String status = str(body, "status");
        String action = str(body, "action");
        if ("APPROVED".equalsIgnoreCase(status)) {
            service.approve(realId);
        } else if ("PAID".equalsIgnoreCase(status) || "PAY".equalsIgnoreCase(action)
                || Boolean.TRUE.equals(body.get("pay"))) {
            service.pay(realId);
        } else {
            return Result.error("不支持的订单状态流转：" + status);
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
