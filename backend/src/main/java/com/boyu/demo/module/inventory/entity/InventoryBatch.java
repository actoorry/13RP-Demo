package com.boyu.demo.module.inventory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

/**
 * 批号管理（inventory_batch）：创建日期当天唯一。
 */
@Data
@TableName("inventory_batch")
public class InventoryBatch {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String batchNo;

    private String productName;

    /** 创建日期（当天唯一）。 */
    private LocalDate createDate;

    private String creator;

    private String remark;
}
