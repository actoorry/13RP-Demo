package com.boyu.demo.module.finance.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.common.SecurityUtils;
import com.boyu.demo.module.finance.entity.Invoice;
import com.boyu.demo.module.finance.mapper.InvoiceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 发票管理服务。
 * <p>状态机：已新增 CREATED → 已审核 APPROVED → 已作废 VOID；
 * 反审核 APPROVED→CREATED；VOID 为终态不可再审核。
 */
@Service
public class InvoiceService extends ServiceImpl<InvoiceMapper, Invoice> {

    /** 审核（仅已新增 CREATED 的发票可审核；VOID 不可再审核）。 */
    @Transactional
    public void approve(Long id) {
        Invoice invoice = getById(id);
        if (invoice == null) {
            throw new IllegalStateException("发票不存在");
        }
        if (!"CREATED".equals(invoice.getStatus())) {
            throw new IllegalStateException("仅已新增(CREATED)发票可审核，当前状态：" + invoice.getStatus());
        }
        invoice.setStatus("APPROVED");
        invoice.setAuditor(SecurityUtils.currentAccount());
        updateById(invoice);
    }

    /** 反审核（仅已审核 APPROVED 的发票可反审核，恢复为 CREATED）。 */
    @Transactional
    public void reject(Long id) {
        Invoice invoice = getById(id);
        if (invoice == null) {
            throw new IllegalStateException("发票不存在");
        }
        if (!"APPROVED".equals(invoice.getStatus())) {
            throw new IllegalStateException("仅已审核(APPROVED)发票可反审核，当前状态：" + invoice.getStatus());
        }
        invoice.setStatus("CREATED");
        invoice.setAuditor(null);
        updateById(invoice);
    }

    /** 作废（已新增或已审核均可作废；VOID 终态不可重复作废）。 */
    @Transactional
    public void voidInvoice(Long id) {
        Invoice invoice = getById(id);
        if (invoice == null) {
            throw new IllegalStateException("发票不存在");
        }
        if ("VOID".equals(invoice.getStatus())) {
            throw new IllegalStateException("发票已作废，请勿重复作废");
        }
        invoice.setStatus("VOID");
        updateById(invoice);
    }
}
