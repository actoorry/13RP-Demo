package com.boyu.demo.module.purchase.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.purchase.entity.Debt;
import com.boyu.demo.module.purchase.mapper.DebtMapper;
import org.springframework.stereotype.Service;

/**
 * 进项欠票服务（一入库单一欠票；一键生成/新增+关联）。
 */
@Service
public class DebtService extends ServiceImpl<DebtMapper, Debt> {
}
