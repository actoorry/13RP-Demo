package com.boyu.demo.module.org.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 我的客户（org_my_customer）：个人负责。
 */
@Data
@TableName("org_my_customer")
public class OrgMyCustomer {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 负责人 id。 */
    private Long ownerId;

    private Long customerId;

    private String customerName;

    /** 关系（主客/次客/主潜/次潜/大中/主供/次供）。 */
    private String relation;
}
