package com.boyu.demo.module.inventory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 安全库存设计（inventory_safe_stock）：有货率/Z 值/补货周期/经济量/订货点/最大/安全。
 */
@Data
@TableName("inventory_safe_stock")
public class InventorySafeStock {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String productName;

    private String material;

    private Long orgId;

    /** 有货率（%）。 */
    private BigDecimal serviceLevel;

    /** Z 值。 */
    private BigDecimal zValue;

    /** 补货周期（天）。 */
    private Integer replenishCycle;

    /** 经济补货量。 */
    private BigDecimal economicQty;

    /** 订货点量。 */
    private BigDecimal orderPointQty;

    /** 最大库存。 */
    private BigDecimal maxQty;

    /** 安全库存。 */
    private BigDecimal safeStock;
}
