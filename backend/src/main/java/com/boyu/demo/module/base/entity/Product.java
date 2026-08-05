package com.boyu.demo.module.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 产品主数据（base_product）：品名→牌号→材质元素 六项维护（品名/牌号/材质/规格/品牌产地/其他）。
 * <p>作废级联：作废父级时其下级数据都会被作废。
 */
@Data
@TableName("base_product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 账套 id（数据隔离边界）。 */
    private Long accountId;

    /** 品名。 */
    private String name;

    /** 牌号。 */
    private String grade;

    /** 材质（材质元素关联）。 */
    private String material;

    /** 规格。 */
    private String spec;

    /** 品牌/产地。 */
    private String brandOrigin;

    /** 其他。 */
    private String other;

    /** 父级 id：0 为品名根节点，品名→牌号→材质树。 */
    private Long parentId;

    private Integer sort;

    /** 状态：1 正常 / 0 作废。 */
    private Integer status;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;
}
