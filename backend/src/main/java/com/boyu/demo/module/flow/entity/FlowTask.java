package com.boyu.demo.module.flow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 流程待办/已办（flow_task）。
 * <p>状态 PENDING → DONE；审批联动由 {@code FlowTaskService#approve} 驱动，
 * 多办理人任一通过即推进实例当前步骤。
 */
@Data
@TableName("flow_task")
public class FlowTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 流程实例 id（x5 或 anma 实例）。 */
    private Long instanceId;

    /** 步骤名称。 */
    private String stepName;

    /** 办理人。 */
    private String assignee;

    /** PENDING / DONE。 */
    private String status;

    /** 备注（审批意见）。 */
    private String remark;
}
