package com.boyu.demo.module.finance.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.finance.entity.Arrival;
import com.boyu.demo.module.finance.mapper.ArrivalMapper;
import org.springframework.stereotype.Service;

/**
 * 到账公告服务。
 */
@Service
public class ArrivalService extends ServiceImpl<ArrivalMapper, Arrival> {
}
