package com.boyu.demo.module.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 发票管理（finance_invoice）。
 * <p>状态机：已新增 CREATED → 已审核 APPROVED → 已作废 VOID；
 * 支持反审核 APPROVED→CREATED；VOID 为终态不可再审核。
 */
@Data
@TableName("finance_invoice")
public class Invoice {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String invoiceNo;

    /** 进项 / 销项。 */
    private String invoiceType;

    /** 客户 id。 */
    private Long customerId;

    private String productCode;

    private String productName;

    /** 金额。 */
    private BigDecimal amount;

    /** CREATED / APPROVED / VOID。 */
    private String status;

    /** 审核人。 */
    private String auditor;
}
