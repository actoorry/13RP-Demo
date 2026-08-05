package com.boyu.demo.module.crm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 活动管理（crm_activity）：使用/生产/经营，主客/次客等关系。
 */
@Data
@TableName("crm_activity")
public class Activity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 客户 id。 */
    private Long customerId;

    /** 联系人 id。 */
    private Long contactId;

    /** 使用/生产/经营。 */
    private String activityType;

    /** 主客/次客/主潜/次潜/大中/主供/次供。 */
    private String relation;

    private String productName;

    /** 价格。 */
    private BigDecimal price;

    /** 预需时间（前端日期控件用 yyyy-MM-dd HH:mm:ss，须显式指定格式）。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime preNeedTime;

    /** 内容。 */
    private String content;

    private String creator;
}
