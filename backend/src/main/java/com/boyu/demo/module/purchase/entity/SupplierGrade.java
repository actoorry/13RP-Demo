package com.boyu.demo.module.purchase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 供应商分级（purchase_supplier_grade）：战略/优选/考察/一般。
 */
@Data
@TableName("purchase_supplier_grade")
public class SupplierGrade {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long supplierId;

    private String supplierName;

    /** 战略/优选/考察/一般。 */
    private String grade;

    private String remark;

    private LocalDateTime createTime;
}
