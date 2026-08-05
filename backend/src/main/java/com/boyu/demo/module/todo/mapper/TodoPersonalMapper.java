package com.boyu.demo.module.todo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyu.demo.module.todo.entity.TodoPersonal;
import org.apache.ibatis.annotations.Mapper;

/**
 * 个人待办 Mapper。
 */
@Mapper
public interface TodoPersonalMapper extends BaseMapper<TodoPersonal> {
}
