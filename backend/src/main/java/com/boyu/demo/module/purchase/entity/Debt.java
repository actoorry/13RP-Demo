package com.boyu.demo.module.purchase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 进项欠票（purchase_debt）：一入库单一欠票；一键生成 / 新增+关联。
 */
@Data
@TableName("purchase_debt")
public class Debt {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String inboundNo;

    private Long inboundId;

    private String invoiceNo;

    private Long invoiceId;

    private Long supplierId;

    private String supplierName;

    private BigDecimal amount;

    /** OPEN 未开 / SETTLED 已开。 */
    private String status;

    private LocalDateTime createTime;
}
