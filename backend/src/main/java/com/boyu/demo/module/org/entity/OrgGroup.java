package com.boyu.demo.module.org.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 组管理（org_group）。
 */
@Data
@TableName("org_group")
public class OrgGroup {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String groupName;

    /** 负责人 id。 */
    private Long ownerId;

    /** 负责人姓名。 */
    private String ownerName;

    private LocalDateTime createTime;
}
