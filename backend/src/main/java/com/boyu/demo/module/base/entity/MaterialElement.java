package com.boyu.demo.module.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 材质元素（base_material_element）：元素符号/常用值/含量区间/牌号独立标记。
 */
@Data
@TableName("base_material_element")
public class MaterialElement {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 元素符号（如 La/Dy/Nd）。 */
    private String symbol;

    private Integer sort;

    /** 常用值/含量。 */
    private String commonValue;

    /** 含量区间下限。 */
    private BigDecimal rangeMin;

    /** 含量区间上限。 */
    private BigDecimal rangeMax;

    /** 牌号"材质元素独立"勾选。 */
    private Integer gradeIndependent;

    private String remark;
}
