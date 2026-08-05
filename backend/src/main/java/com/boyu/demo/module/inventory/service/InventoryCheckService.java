package com.boyu.demo.module.inventory.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.inventory.entity.InventoryCheck;
import com.boyu.demo.module.inventory.mapper.InventoryCheckMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 盘点服务：状态机 制单 CREATED → 批准 APPROVED → 保管员审核 CHECKED（与入库同模式）。
 * <p>前置校验：未 APPROVED 不可 CHECKED；已 CHECKED 不可再操作；非法迁移抛异常拒绝。
 */
@Service
public class InventoryCheckService extends ServiceImpl<InventoryCheckMapper, InventoryCheck> {

    /** 批准（仅 CREATED 可批准）。 */
    @Transactional
    public void approve(Long id) {
        InventoryCheck check = require(id);
        if (!"CREATED".equals(check.getStatus())) {
            throw new IllegalStateException("仅制单（CREATED）状态的盘点单可批准，当前：" + check.getStatus());
        }
        check.setStatus("APPROVED");
        updateById(check);
    }

    /** 保管员审核（仅 APPROVED 可审核；已 CHECKED 不可再操作）。 */
    @Transactional
    public void check(Long id) {
        InventoryCheck check = require(id);
        if (!"APPROVED".equals(check.getStatus())) {
            throw new IllegalStateException("未批准的盘点单不可审核，当前：" + check.getStatus());
        }
        check.setStatus("CHECKED");
        updateById(check);
    }

    private InventoryCheck require(Long id) {
        InventoryCheck check = getById(id);
        if (check == null) {
            throw new IllegalStateException("盘点单不存在：id=" + id);
        }
        return check;
    }
}
