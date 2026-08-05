package com.boyu.demo.module.todo.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.todo.entity.TodoPersonal;
import com.boyu.demo.module.todo.mapper.TodoPersonalMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 个人待办服务：公共/指派；状态 PENDING → DONE。
 */
@Service
public class TodoPersonalService extends ServiceImpl<TodoPersonalMapper, TodoPersonal> {

    /** 完成待办（仅待办 PENDING 可完成）。 */
    @Transactional
    public void done(Long id) {
        TodoPersonal todo = getById(id);
        if (todo == null || !"PENDING".equals(todo.getStatus())) {
            throw new IllegalStateException("待办不存在或已完成，不可重复办理");
        }
        todo.setStatus("DONE");
        updateById(todo);
    }
}
