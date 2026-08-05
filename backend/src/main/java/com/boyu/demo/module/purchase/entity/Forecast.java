package com.boyu.demo.module.purchase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 预测预案（purchase_forecast）：年规划 YEAR / 月计划 MONTH / 周优化 WEEK / 日执行 DAY。
 */
@Data
@TableName("purchase_forecast")
public class Forecast {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** YEAR / MONTH / WEEK / DAY。 */
    private String planType;

    private String planName;

    private LocalDate periodStart;

    private LocalDate periodEnd;

    private BigDecimal forecastValue;

    private String creator;
}
