package com.boyu.demo.module.sale.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

/**
 * 业务日报漏斗（sale_daily_report）：联系家数 → 线索家数（报价）→ 成交家数（开出库单）。
 */
@Data
@TableName("sale_daily_report")
public class SaleDailyReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDate reportDate;

    /** 联系家数。 */
    private Integer contactCnt;

    /** 销售线索家数（=报价家数）。 */
    private Integer leadCnt;

    /** 成交家数（=开出库单客户数）。 */
    private Integer dealCnt;

    private Long orgId;
}
