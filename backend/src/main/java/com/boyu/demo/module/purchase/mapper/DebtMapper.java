package com.boyu.demo.module.purchase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyu.demo.module.purchase.entity.Debt;
import org.apache.ibatis.annotations.Mapper;

/**
 * 进项欠票 Mapper。
 */
@Mapper
public interface DebtMapper extends BaseMapper<Debt> {
}
