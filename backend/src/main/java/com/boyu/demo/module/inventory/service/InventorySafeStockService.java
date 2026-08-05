package com.boyu.demo.module.inventory.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.inventory.entity.InventorySafeStock;
import com.boyu.demo.module.inventory.mapper.InventorySafeStockMapper;
import org.springframework.stereotype.Service;

/**
 * 安全库存设计服务。
 */
@Service
public class InventorySafeStockService extends ServiceImpl<InventorySafeStockMapper, InventorySafeStock> {
}
