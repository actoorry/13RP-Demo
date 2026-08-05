package com.boyu.demo.module.purchase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyu.demo.module.purchase.entity.Forecast;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预测预案 Mapper。
 */
@Mapper
public interface ForecastMapper extends BaseMapper<Forecast> {
}
