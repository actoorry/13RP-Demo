package com.boyu.demo.module.inventory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 库存统计（inventory_stock）。
 * <p>库龄 stock_age ≥ age_warn_days 时列表返回 {@code warn=true}（红色预警），非表字段由查询时填充。
 */
@Data
@TableName("inventory_stock")
public class InventoryStock {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String productName;

    private String grade;

    private String spec;

    private Long orgId;

    private BigDecimal actualQty;

    private BigDecimal transitQty;

    /** 库龄（天）。 */
    private Integer stockAge;

    /** 库龄预警阈值（默认 15 天）。 */
    private Integer ageWarnDays;

    /** 库龄预警标记（非表字段，列表查询时计算：stockAge ≥ ageWarnDays）。 */
    @TableField(exist = false)
    private Boolean warn;
}
