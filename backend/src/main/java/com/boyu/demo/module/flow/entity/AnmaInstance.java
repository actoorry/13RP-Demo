package com.boyu.demo.module.flow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 安码流程实例（flow_anma_instance）：合同/财务审批。
 * <p>状态 RUNNING → DONE / REJECTED。
 */
@Data
@TableName("flow_anma_instance")
public class AnmaInstance {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 流程单号。 */
    private String flowNo;

    /** 合同/财务审批。 */
    private String flowType;

    private String title;

    /** 合同金额。 */
    private BigDecimal contractAmount;

    /** 供应商 id。 */
    private Long supplierId;

    /** 客户 id。 */
    private Long customerId;

    /** 当前步骤。 */
    private String currentStep;

    /** 当前审批人。 */
    private String approver;

    /** 供应商名称（非表字段，列表联表填充）。 */
    @TableField(exist = false)
    private String supplierName;

    /** 客户名称（非表字段，列表联表填充）。 */
    @TableField(exist = false)
    private String customerName;

    /** RUNNING / DONE / REJECTED。 */
    private String status;
}
