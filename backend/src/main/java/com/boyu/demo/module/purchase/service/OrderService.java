package com.boyu.demo.module.purchase.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.purchase.entity.PurchaseOrder;
import com.boyu.demo.module.purchase.mapper.PurchaseOrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 待审批订单服务 + 结算分流。
 * <p>审批通过后按结算方式分流：现款后货 → 待付款 WAIT_PAY →（付款）→ 待入库 WAIT_INBOUND；
 * 先货后款 → 直接待入库 WAIT_INBOUND。
 */
@Service
public class OrderService extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder> {

    /** 审批通过 + 结算分流（仅待审批 PENDING_APPROVE 的订单可审批）。 */
    @Transactional
    public void approve(Long id) {
        PurchaseOrder order = getById(id);
        if (order == null || !"PENDING_APPROVE".equals(order.getStatus())) {
            return;
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
        PurchaseOrder order = getById(id);
        if (order == null || !"WAIT_PAY".equals(order.getSettlementStatus())) {
            return;
        }
        order.setSettlementStatus("WAIT_INBOUND");
        updateById(order);
    }
}
