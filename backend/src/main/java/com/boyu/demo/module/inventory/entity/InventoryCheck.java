package com.boyu.demo.module.inventory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 盘点（inventory_check）：状态机 制单完成 CREATED → 批准 APPROVED → 保管员审核 CHECKED（与入库同模式）。
 */
@Data
@TableName("inventory_check")
public class InventoryCheck {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String checkNo;

    private String batchNo;

    /** 实盘数量。 */
    private BigDecimal actualQty;

    /** 状态机：制单完成 CREATED → 批准 APPROVED → 保管员审核 CHECKED（非法迁移由 Service 拒绝）。 */
    private String status;
}
