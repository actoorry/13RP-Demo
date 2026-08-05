package com.boyu.demo.module.inventory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 出库管理/发货（inventory_outbound）：状态机 制单 CREATED → 批准 APPROVED；
 * 运费承担方：博宇承担/对方承担。
 */
@Data
@TableName("inventory_outbound")
public class InventoryOutbound {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String outboundNo;

    private String saleOrderNo;

    private String productName;

    private BigDecimal qty;

    /** 运费承担方：博宇承担/对方承担。 */
    private String freightBearer;

    /** 承运方。 */
    private String carrier;

    /** 车牌（≤7 位）。 */
    private String plateNo;

    /** 司机（≤5 位）。 */
    private String driver;

    /** 司机电话（≤11 位）。 */
    private String driverPhone;

    /** 制单完成 CREATED → 批准 APPROVED。 */
    private String status;
}
