package com.boyu.demo.module.crm.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.crm.entity.Activity;
import com.boyu.demo.module.crm.mapper.ActivityMapper;
import org.springframework.stereotype.Service;

/**
 * 活动管理服务。
 */
@Service
public class ActivityService extends ServiceImpl<ActivityMapper, Activity> {
}
