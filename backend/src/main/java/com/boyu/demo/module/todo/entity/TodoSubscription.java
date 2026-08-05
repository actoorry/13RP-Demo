package com.boyu.demo.module.todo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 四板块订阅（todo_subscription）：CRM/采购/销售/财务，config_json 存阀值 JSON 字符串。
 */
@Data
@TableName("todo_subscription")
public class TodoSubscription {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** CRM/采购/销售/财务。 */
    private String boardType;

    /** 订阅类型。 */
    private String subType;

    /** 配置（阀值）JSON 字符串。 */
    private String configJson;

    /** 订阅人 id。 */
    private Long ownerId;

    /** 1 启用 / 0 停用。 */
    private Integer enabled;
}
