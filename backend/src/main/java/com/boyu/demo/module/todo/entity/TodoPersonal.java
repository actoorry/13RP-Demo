package com.boyu.demo.module.todo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 个人待办（todo_personal）：公共/指派；出库/入库模板；状态 PENDING → DONE。
 */
@Data
@TableName("todo_personal")
public class TodoPersonal {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 id。 */
    private Long userId;

    /** 待办类型：公共/指派。 */
    private String todoType;

    /** 出库/入库模板。 */
    private String templateType;

    /** 提醒时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime remindTime;

    /** 指派人员。 */
    private String assignee;

    /** PENDING / DONE。 */
    private String status;
}
