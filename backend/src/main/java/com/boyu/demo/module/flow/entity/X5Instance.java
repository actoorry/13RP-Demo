package com.boyu.demo.module.flow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * X5 流程实例（flow_x5_instance）：报销/借款/付款/退款。
 * <p>500 元分级审批：金额 &lt;500 单级审批，≥500 走多级；状态 RUNNING → DONE / REJECTED。
 */
@Data
@TableName("flow_x5_instance")
public class X5Instance {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 流程单号。 */
    private String flowNo;

    /** 报销/借款/付款/退款。 */
    private String flowType;

    private String title;

    /** 金额（500 元分级审批依据）。 */
    private BigDecimal amount;

    /** 申请人。 */
    private String applicant;

    /** 当前步骤。 */
    private String currentStep;

    /** 当前审批人。 */
    private String approver;

    /** RUNNING / DONE / REJECTED。 */
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
