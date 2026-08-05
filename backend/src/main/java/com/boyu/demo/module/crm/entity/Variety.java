package com.boyu.demo.module.crm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 品种资料（crm_variety）：月用量/下月计划/竞争对手/SWOT。
 */
@Data
@TableName("crm_variety")
public class Variety {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 客户 id。 */
    private Long customerId;

    /** 使用/生产/经营。 */
    private String varietyType;

    private String productName;

    private String grade;

    private String material;

    private String spec;

    /** 品牌/产地。 */
    private String brandOrigin;

    /** 竞争对手。 */
    private String competitor;

    /** SWOT。 */
    private String swot;

    /** 月用量。 */
    private BigDecimal monthlyQty;

    /** 下月计划。 */
    private BigDecimal nextMonthPlan;
}
