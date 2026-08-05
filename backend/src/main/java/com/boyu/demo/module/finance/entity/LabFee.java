package com.boyu.demo.module.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 化验费（finance_lab_fee）。
 * <p>报告状态机：未上传 PENDING → 合格 PASS / 不合格 FAIL；
 * 付款状态机：未付款 UNPAID → 已付款 PAID → 已冲账 REIMBURSED。
 * 前置校验：报告未 PASS 前不可付款；未付款不可冲账。
 */
@Data
@TableName("finance_lab_fee")
public class LabFee {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 入库单 id。 */
    private Long inboundId;

    /** 化验机构。 */
    private String labName;

    /** 样品编号。 */
    private String sampleNo;

    /** 化验元素。 */
    private String element;

    /** 化验费。 */
    private BigDecimal labFee;

    /** PENDING 未上传 / PASS 合格 / FAIL 不合格。 */
    private String reportStatus;

    /** UNPAID 未付款 / PAID 已付款 / REIMBURSED 已冲账。 */
    private String payStatus;

    /** 凭证号。 */
    private String voucherNo;
}
