package com.boyu.demo.module.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyu.demo.module.finance.entity.Expense;
import org.apache.ibatis.annotations.Mapper;

/**
 * 费用管理 Mapper。
 */
@Mapper
public interface ExpenseMapper extends BaseMapper<Expense> {
}
