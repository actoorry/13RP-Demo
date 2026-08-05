package com.boyu.demo.module.org.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 组内客户（org_group_customer）：组级共享。
 */
@Data
@TableName("org_group_customer")
public class OrgGroupCustomer {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long groupId;

    private Long customerId;

    private String customerName;

    /** 关系（主客/次客/主潜/次潜/大中/主供/次供）。 */
    private String relation;
}
