package com.boyu.demo.module.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 账套（base_account）：多账套切换，数据隔离边界。
 */
@Data
@TableName("base_account")
public class Account {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 账套名称（博宇股份/藏博/沈博/总部）。 */
    private String name;

    /** 账套编码。 */
    private String code;

    /** 状态：1 启用 / 0 停用。 */
    private Integer status;

    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
