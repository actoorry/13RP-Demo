package com.boyu.demo.module.crm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 销售线索（crm_lead）。
 */
@Data
@TableName("crm_lead")
public class Lead {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String source;

    /** 公司类型。 */
    private String companyType;

    private String phone;

    private String tel;

    private String email;

    private String address;

    private String industry;

    /** 等级。 */
    private String level;

    /** 下次联系时间（前端日期控件用 yyyy-MM-dd HH:mm:ss，须显式指定格式）。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime nextContactTime;

    private String remark;

    /** 负责人 id。 */
    private Long ownerId;

    /** 跟进标记。 */
    private Integer followFlag;

    /** 已转化。 */
    private Integer convertedFlag;

    /** 最后跟进时间（前端日期控件用 yyyy-MM-dd HH:mm:ss，须显式指定格式）。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastFollowTime;

    /** 扩展字段（JSON）。 */
    private String extraFields;
}
