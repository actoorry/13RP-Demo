package com.boyu.demo.module.inventory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.inventory.entity.InventoryTransfer;
import com.boyu.demo.module.inventory.service.InventoryTransferService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 调拨接口：GET/POST/PUT/DELETE /api/inventory/transfer（库位转移）。
 */
@RestController
@RequestMapping("/api/inventory/transfer")
public class TransferController {

    private final InventoryTransferService service;

    public TransferController(InventoryTransferService service) {
        this.service = service;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<InventoryTransfer> w = new LambdaQueryWrapper<>();
        w.and(keyword != null && !keyword.isBlank(),
                        wq -> wq.like(InventoryTransfer::getTransferNo, keyword)
                                .or().like(InventoryTransfer::getBatchNo, keyword)
                                .or().like(InventoryTransfer::getTargetLocation, keyword))
                .orderByDesc(InventoryTransfer::getId);
        return Result.ok(PageQuery.toPageMap(service.page(new Page<>(query.getPage(), query.getSize()), w)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('inventory:transfer:add')")
    public Result<Void> create(@RequestBody InventoryTransfer entity) {
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus("CREATED");
        }
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:transfer:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody InventoryTransfer entity) {
        entity.setId(id);
        service.updateById(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:transfer:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }
}
