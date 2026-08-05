package com.boyu.demo.module.purchase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyu.demo.module.purchase.entity.Payable;
import org.apache.ibatis.annotations.Mapper;

/**
 * 应付列表 Mapper。
 */
@Mapper
public interface PayableMapper extends BaseMapper<Payable> {
}
