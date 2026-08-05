package com.boyu.demo.module.inventory.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.inventory.entity.InventoryStock;
import com.boyu.demo.module.inventory.mapper.InventoryStockMapper;
import org.springframework.stereotype.Service;

/**
 * 库存统计服务。
 */
@Service
public class InventoryStockService extends ServiceImpl<InventoryStockMapper, InventoryStock> {
}
