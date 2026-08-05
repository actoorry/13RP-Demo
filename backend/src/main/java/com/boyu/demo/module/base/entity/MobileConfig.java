package com.boyu.demo.module.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 移动端主营品种配置（base_mobile_config）：按品种系列分类。
 */
@Data
@TableName("base_mobile_config")
public class MobileConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 主营品种。 */
    private String productName;

    private Integer sort;

    /** 状态：1 启用 / 0 停用。 */
    private Integer status;
}
