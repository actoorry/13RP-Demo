package com.boyu.demo.module.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 应收应付（finance_ar_ap）。
 */
@Data
@TableName("finance_ar_ap")
public class ArAp {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 往来方类型：客户 CUSTOMER / 供应商 SUPPLIER。 */
    private String partyType;

    /** 往来方 id。 */
    private Long partyId;

    /** 账套 id。 */
    private Long accountId;

    /** 所属组织 id。 */
    private Long orgId;

    /** 应收。 */
    private BigDecimal receivable;

    /** 应付。 */
    private BigDecimal payable;

    /** 余额。 */
    private BigDecimal balance;
}
