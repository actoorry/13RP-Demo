package com.boyu.demo.module.purchase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 询价管理（purchase_inquiry）：急询价 URGENT / 指定询价 SPECIFIED。
 * <p>状态机：发起 CREATED → 接收 RECEIVED → 反馈 REPLIED。
 */
@Data
@TableName("purchase_inquiry")
public class Inquiry {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String inquiryNo;

    /** URGENT 急询价 / SPECIFIED 指定询价。 */
    private String inquiryType;

    private String productName;

    private BigDecimal productQty;

    private Long supplierId;

    private String supplierName;

    /** CREATED / RECEIVED / REPLIED。 */
    private String status;

    /** 是否标"急"。 */
    private Integer urgentFlag;

    private LocalDateTime replyTime;

    private String creator;
}
