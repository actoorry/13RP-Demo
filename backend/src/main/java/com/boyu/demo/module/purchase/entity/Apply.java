package com.boyu.demo.module.purchase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 采购申请审批链（purchase_apply）：批准→复核两段审批。
 * <p>状态机：待批准 PENDING_APPROVE → 已批准 APPROVED → 待复核 PENDING_REVIEW → 已复核 REVIEWED。
 */
@Data
@TableName("purchase_apply")
public class Apply {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String applyNo;

    /** 关联询价单 id。 */
    private Long inquiryId;

    private String applicant;

    /** PENDING_APPROVE / APPROVED / PENDING_REVIEW / REVIEWED。 */
    private String status;

    private String approver;

    private LocalDateTime approveTime;

    private String reviewer;

    private LocalDateTime reviewTime;
}
