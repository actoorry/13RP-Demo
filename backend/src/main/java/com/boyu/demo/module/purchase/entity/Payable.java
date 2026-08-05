package com.boyu.demo.module.purchase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 应付列表（purchase_payable）：按品种负责人权限显示，匹配应付余额直接生成付款单。
 */
@Data
@TableName("purchase_payable")
public class Payable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long supplierId;

    private String supplierName;

    private BigDecimal balance;

    private LocalDate dueDate;

    /** OPEN 未付 / PAID 已付。 */
    private String status;
}
