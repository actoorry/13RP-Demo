package com.boyu.demo.module.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyu.demo.module.finance.entity.Arrival;
import org.apache.ibatis.annotations.Mapper;

/**
 * 到账公告 Mapper。
 */
@Mapper
public interface ArrivalMapper extends BaseMapper<Arrival> {
}
