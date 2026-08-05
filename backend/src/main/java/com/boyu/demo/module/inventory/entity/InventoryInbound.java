package com.boyu.demo.module.inventory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 入库管理（inventory_inbound）：估价/代销/内部；状态机 制单 CREATED → 批准 APPROVED → 保管员审核 CHECKED。
 */
@Data
@TableName("inventory_inbound")
public class InventoryInbound {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String inboundNo;

    /** 估价/代销/内部。 */
    private String inboundType;

    private String sourceOrderNo;

    private String productName;

    private BigDecimal qty;

    /** 账面结算数量。 */
    private BigDecimal settleQty;

    /** 制单完成 CREATED → 批准 APPROVED → 保管员审核 CHECKED。 */
    private String status;

    /** 审核人。 */
    private String checker;

    /** 分级审核：≤合理称差直接审核 / >合理称差总监/经理审核。 */
    private String auditLevel;
}
