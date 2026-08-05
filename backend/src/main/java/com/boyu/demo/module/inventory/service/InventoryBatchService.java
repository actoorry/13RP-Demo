package com.boyu.demo.module.inventory.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.inventory.entity.InventoryBatch;
import com.boyu.demo.module.inventory.mapper.InventoryBatchMapper;
import org.springframework.stereotype.Service;

/**
 * 批号管理服务。
 */
@Service
public class InventoryBatchService extends ServiceImpl<InventoryBatchMapper, InventoryBatch> {
}
