package com.boyu.demo.module.sale.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyu.demo.module.sale.entity.SaleOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 销售订单 Mapper。
 */
@Mapper
public interface SaleOrderMapper extends BaseMapper<SaleOrder> {
}
