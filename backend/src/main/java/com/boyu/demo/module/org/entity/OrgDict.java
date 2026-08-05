package com.boyu.demo.module.org.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 组织/岗位字典（org_dict）。
 */
@Data
@TableName("org_dict")
public class OrgDict {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 字典类型：org 组织 / position 岗位。 */
    private String dictType;

    private String name;

    /** 父级 id（组织树）。 */
    private Long parentId;

    private Integer sort;
}
