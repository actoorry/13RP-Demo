package com.boyu.demo.module.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 合同包装验收标准（base_package_standard）。
 */
@Data
@TableName("base_package_standard")
public class PackageStandard {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 包装名称（袋装/纸箱/托盘等）。 */
    private String packageName;

    /** 破损赔偿。 */
    private String damageCompensation;

    /** 状态：1 启用 / 0 停用。 */
    private Integer status;
}
