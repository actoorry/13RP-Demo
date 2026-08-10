package com.boyu.demo.module.purchase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 待审批订单 + 结算分流（purchase_order）。
 * <p>审批后按结算方式分流：现款后货 → 待付款 WAIT_PAY → 待入库 WAIT_INBOUND；先货后款 → 待入库 WAIT_INBOUND。
 */
@Data
@TableName("purchase_order")
public class PurchaseOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    /** 来源：供应商活动/销采部/客服部我要采购。 */
    private String source;

    /** 结算方式：现款后货 / 先货后款。 */
    private String settleMethod;

    private Long supplierId;

    private String supplierName;

    private String productName;

    private BigDecimal qty;

    private BigDecimal payAmount;

    /** 待审批 PENDING_APPROVE → 已审批 APPROVED。 */
    private String status;

    /** 结算分流：WAIT_PAY 待付款 / WAIT_INBOUND 待入库。 */
    private String settlementStatus;

    private String creator;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
