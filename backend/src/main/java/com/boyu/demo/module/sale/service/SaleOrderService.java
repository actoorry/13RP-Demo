package com.boyu.demo.module.sale.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.sale.entity.SaleOrder;
import com.boyu.demo.module.sale.mapper.SaleOrderMapper;
import org.springframework.stereotype.Service;

/**
 * 销售订单服务。
 */
@Service
public class SaleOrderService extends ServiceImpl<SaleOrderMapper, SaleOrder> {
}
