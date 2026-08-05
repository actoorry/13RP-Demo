package com.boyu.demo.module.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 费用管理（finance_expense）。
 * <p>分摊状态机：未分摊 UNALLOCATED → 已分摊 ALLOCATED（不可逆，已分摊不可再分摊）；
 * marked 用于前端标红（0/1）。
 */
@Data
@TableName("finance_expense")
public class Expense {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String expenseNo;

    /** 客户 id。 */
    private Long customerId;

    private String productName;

    /** 费用金额。 */
    private BigDecimal amount;

    /** 税额。 */
    private BigDecimal taxAmount;

    /** 分摊类型。 */
    private String allocateType;

    /** UNALLOCATED 未分摊 / ALLOCATED 已分摊。 */
    private String allocateStatus;

    /** 标记（变色红色）：0 未标 / 1 已标。 */
    private Integer marked;
}
