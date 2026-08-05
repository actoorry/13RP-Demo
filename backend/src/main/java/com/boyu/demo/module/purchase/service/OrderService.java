package com.boyu.demo.module.purchase.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.purchase.entity.PurchaseOrder;
import com.boyu.demo.module.purchase.mapper.PurchaseOrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 待审批订单服务 + 结算分流 + 关闭订单。
 * <p>审批通过后按结算方式分流：现款后货 → 待付款 WAIT_PAY →（付款）→ 待入库 WAIT_INBOUND；
 * 先货后款 → 直接待入库 WAIT_INBOUND。关闭订单为终态，关闭后不可审批/付款。
 * 记录不存在 / 状态非法时抛 IllegalStateException（Controller 捕获转 Result.error）。
 */
@Service
public class OrderService extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder> {

    /** 审批通过 + 结算分流（仅待审批 PENDING_APPROVE 的订单可审批）。 */
    @Transactional
    public void approve(Long id) {
        PurchaseOrder order = require(id);
        if (!"PENDING_APPROVE".equals(order.getStatus())) {
            throw new IllegalStateException("仅待审批(PENDING_APPROVE)状态的采购订单可审批，当前：" + order.getStatus());
        }
        order.setStatus("APPROVED");
        if ("现款后货".equals(order.getSettleMethod())) {
            order.setSettlementStatus("WAIT_PAY");
        } else {
            order.setSettlementStatus("WAIT_INBOUND");
        }
        updateById(order);
    }

    /** 现款后货付款完成 → 待入库（仅待付款 WAIT_PAY 的订单可付款）。 */
    @Transactional
    public void pay(Long id) {
        PurchaseOrder order = require(id);
        if (!"WAIT_PAY".equals(order.getSettlementStatus())) {
            throw new IllegalStateException("仅待付款(WAIT_PAY)结算状态的采购订单可付款，当前：" + order.getSettlementStatus());
        }
        order.setSettlementStatus("WAIT_INBOUND");
        updateById(order);
    }

    /** 关闭订单（仅待审批 PENDING_APPROVE 或已审批 APPROVED 状态的订单可关闭；终态，关闭后不可审批/付款）。 */
    @Transactional
    public void close(Long id) {
        PurchaseOrder order = require(id);
        if (!"PENDING_APPROVE".equals(order.getStatus()) && !"APPROVED".equals(order.getStatus())) {
            throw new IllegalStateException("仅待审批/已审批状态的采购订单可关闭，当前：" + order.getStatus());
        }
        order.setStatus("CLOSED");
        updateById(order);
    }

    private PurchaseOrder require(Long id) {
        PurchaseOrder order = getById(id);
        if (order == null) {
            throw new IllegalStateException("采购订单不存在：id=" + id);
        }
        return order;
    }
}
