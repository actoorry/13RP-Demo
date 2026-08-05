package com.boyu.demo.module.purchase.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.purchase.entity.Forecast;
import com.boyu.demo.module.purchase.mapper.ForecastMapper;
import org.springframework.stereotype.Service;

/**
 * 预测预案服务（年/月/周/日）。
 */
@Service
public class ForecastService extends ServiceImpl<ForecastMapper, Forecast> {
}
