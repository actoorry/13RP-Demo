package com.boyu.demo.module.inventory.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.inventory.entity.InventoryOutbound;
import com.boyu.demo.module.inventory.mapper.InventoryOutboundMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 出库管理服务：状态机 制单 CREATED → 批准 APPROVED。
 * <p>前置校验：仅 CREATED 可批准；非法迁移抛异常拒绝。
 */
@Service
public class InventoryOutboundService extends ServiceImpl<InventoryOutboundMapper, InventoryOutbound> {

    /** 批准出库（仅 CREATED 可批准）。 */
    @Transactional
    public void approve(Long id) {
        InventoryOutbound outbound = require(id);
        if (!"CREATED".equals(outbound.getStatus())) {
            throw new IllegalStateException("仅制单（CREATED）状态的出库单可批准，当前：" + outbound.getStatus());
        }
        outbound.setStatus("APPROVED");
        updateById(outbound);
    }

    private InventoryOutbound require(Long id) {
        InventoryOutbound outbound = getById(id);
        if (outbound == null) {
            throw new IllegalStateException("出库单不存在：id=" + id);
        }
        return outbound;
    }
}
