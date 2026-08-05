package com.boyu.demo.module.finance.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.finance.entity.LabFee;
import com.boyu.demo.module.finance.mapper.LabFeeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 化验费服务。
 * <p>报告状态机：未上传 PENDING → 合格 PASS / 不合格 FAIL（报告上传后不可修改）；
 * 付款状态机：未付款 UNPAID → 已付款 PAID → 已冲账 REIMBURSED。
 * 前置校验：报告未合格 PASS 前不可付款；未付款不可冲账。
 */
@Service
public class LabFeeService extends ServiceImpl<LabFeeMapper, LabFee> {

    /** 上传合格报告（仅未上传 PENDING 可操作）。 */
    @Transactional
    public void reportPass(Long id) {
        LabFee fee = getById(id);
        if (fee == null) {
            throw new IllegalStateException("化验费单不存在");
        }
        if (!"PENDING".equals(fee.getReportStatus())) {
            throw new IllegalStateException("报告已上传，不可重复修改报告状态");
        }
        fee.setReportStatus("PASS");
        updateById(fee);
    }

    /** 上传不合格报告（仅未上传 PENDING 可操作）。 */
    @Transactional
    public void reportFail(Long id) {
        LabFee fee = getById(id);
        if (fee == null) {
            throw new IllegalStateException("化验费单不存在");
        }
        if (!"PENDING".equals(fee.getReportStatus())) {
            throw new IllegalStateException("报告已上传，不可重复修改报告状态");
        }
        fee.setReportStatus("FAIL");
        updateById(fee);
    }

    /** 付款（前置：报告合格 PASS 且未付款 UNPAID；凭证号可选带填）。 */
    @Transactional
    public void pay(Long id, String voucherNo) {
        LabFee fee = getById(id);
        if (fee == null) {
            throw new IllegalStateException("化验费单不存在");
        }
        if (!"PASS".equals(fee.getReportStatus())) {
            throw new IllegalStateException("报告未合格(PASS)，不可付款");
        }
        if (!"UNPAID".equals(fee.getPayStatus())) {
            throw new IllegalStateException("化验费已付款，请勿重复付款");
        }
        fee.setPayStatus("PAID");
        if (voucherNo != null && !voucherNo.isBlank()) {
            fee.setVoucherNo(voucherNo);
        }
        updateById(fee);
    }

    /** 冲账（前置：仅已付款 PAID 可冲账；未付款不可冲账）。 */
    @Transactional
    public void reimburse(Long id, String voucherNo) {
        LabFee fee = getById(id);
        if (fee == null) {
            throw new IllegalStateException("化验费单不存在");
        }
        if (!"PAID".equals(fee.getPayStatus())) {
            throw new IllegalStateException("化验费未付款，不可冲账");
        }
        fee.setPayStatus("REIMBURSED");
        if (voucherNo != null && !voucherNo.isBlank()) {
            fee.setVoucherNo(voucherNo);
        }
        updateById(fee);
    }
}
