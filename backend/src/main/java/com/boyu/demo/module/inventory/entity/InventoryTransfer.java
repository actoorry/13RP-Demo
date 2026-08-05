package com.boyu.demo.module.inventory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 调拨（inventory_transfer）：库位转移。
 */
@Data
@TableName("inventory_transfer")
public class InventoryTransfer {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String transferNo;

    private String batchNo;

    /** 实提数量。 */
    private BigDecimal qty;

    /** 目标库位。 */
    private String targetLocation;

    private String status;
}
