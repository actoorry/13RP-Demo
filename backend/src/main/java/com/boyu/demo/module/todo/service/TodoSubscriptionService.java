package com.boyu.demo.module.todo.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.todo.entity.TodoSubscription;
import com.boyu.demo.module.todo.mapper.TodoSubscriptionMapper;
import org.springframework.stereotype.Service;

/**
 * 四板块订阅服务（CRM/采购/销售/财务，config_json 存阀值）。
 */
@Service
public class TodoSubscriptionService extends ServiceImpl<TodoSubscriptionMapper, TodoSubscription> {
}
