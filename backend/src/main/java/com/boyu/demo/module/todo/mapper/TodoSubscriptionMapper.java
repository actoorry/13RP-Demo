package com.boyu.demo.module.todo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyu.demo.module.todo.entity.TodoSubscription;
import org.apache.ibatis.annotations.Mapper;

/**
 * 四板块订阅 Mapper。
 */
@Mapper
public interface TodoSubscriptionMapper extends BaseMapper<TodoSubscription> {
}
