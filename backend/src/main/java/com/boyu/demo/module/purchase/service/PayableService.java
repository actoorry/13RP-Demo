package com.boyu.demo.module.purchase.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.purchase.entity.Payable;
import com.boyu.demo.module.purchase.mapper.PayableMapper;
import org.springframework.stereotype.Service;

/**
 * 应付列表服务（匹配应付余额直接生成付款单）。
 */
@Service
public class PayableService extends ServiceImpl<PayableMapper, Payable> {
}
