package com.boyu.demo.module.sale.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.sale.entity.SaleInvoiceApply;
import com.boyu.demo.module.sale.mapper.SaleInvoiceApplyMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 开票申请服务：状态机 申请 APPLIED → 待开 PENDING → 已开 ISSUED。
 * <p>前置校验：仅 APPLIED 可进入待开；仅 PENDING 可开票；ISSUED 不可再流转（非法迁移抛异常拒绝）。
 */
@Service
public class SaleInvoiceApplyService extends ServiceImpl<SaleInvoiceApplyMapper, SaleInvoiceApply> {

    /** 申请 → 待开（仅 APPLIED 状态可进入待开）。 */
    @Transactional
    public void toPending(Long id) {
        SaleInvoiceApply apply = require(id);
        if (!"APPLIED".equals(apply.getStatus())) {
            throw new IllegalStateException("仅申请（APPLIED）状态的开票申请可进入待开，当前：" + apply.getStatus());
        }
        apply.setStatus("PENDING");
        updateById(apply);
    }

    /** 待开 → 已开（仅 PENDING 状态可开票；ISSUED 不可再流转）。 */
    @Transactional
    public void issue(Long id) {
        SaleInvoiceApply apply = require(id);
        if (!"PENDING".equals(apply.getStatus())) {
            throw new IllegalStateException("仅待开（PENDING）状态的开票申请可开票，当前：" + apply.getStatus());
        }
        apply.setStatus("ISSUED");
        updateById(apply);
    }

    private SaleInvoiceApply require(Long id) {
        SaleInvoiceApply apply = getById(id);
        if (apply == null) {
            throw new IllegalStateException("开票申请不存在：id=" + id);
        }
        return apply;
    }
}
