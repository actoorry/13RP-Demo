package com.boyu.demo.module.inventory.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.inventory.entity.InventoryTransfer;
import com.boyu.demo.module.inventory.mapper.InventoryTransferMapper;
import org.springframework.stereotype.Service;

/**
 * 调拨服务。
 */
@Service
public class InventoryTransferService extends ServiceImpl<InventoryTransferMapper, InventoryTransfer> {
}
