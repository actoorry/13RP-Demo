package com.boyu.demo.module.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 到账公告（finance_arrival）。
 */
@Data
@TableName("finance_arrival")
public class Arrival {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 账套 id。 */
    private Long accountId;

    /** 所属组织 id。 */
    private Long orgId;

    /** 到账金额。 */
    private BigDecimal amount;

    /** 到账时间（前端日期控件用 yyyy-MM-dd HH:mm:ss，须显式指定格式）。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime arrivalTime;

    /** 操作人。 */
    private String operator;
}
