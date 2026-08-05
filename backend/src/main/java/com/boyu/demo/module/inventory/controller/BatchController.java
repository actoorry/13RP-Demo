package com.boyu.demo.module.inventory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.inventory.entity.InventoryBatch;
import com.boyu.demo.module.inventory.service.InventoryBatchService;
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
 * 批号管理接口：GET/POST/PUT/DELETE /api/inventory/batch。
 */
@RestController
@RequestMapping("/api/inventory/batch")
public class BatchController {

    private final InventoryBatchService service;

    public BatchController(InventoryBatchService service) {
        this.service = service;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String productName,
                                            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<InventoryBatch> w = new LambdaQueryWrapper<>();
        w.like(productName != null && !productName.isBlank(), InventoryBatch::getProductName, productName)
                .and(keyword != null && !keyword.isBlank(),
                        wq -> wq.like(InventoryBatch::getBatchNo, keyword)
                                .or().like(InventoryBatch::getProductName, keyword))
                .orderByDesc(InventoryBatch::getId);
        return Result.ok(PageQuery.toPageMap(service.page(new Page<>(query.getPage(), query.getSize()), w)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('inventory:batch:add')")
    public Result<Void> create(@RequestBody InventoryBatch entity) {
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:batch:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody InventoryBatch entity) {
        entity.setId(id);
        service.updateById(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:batch:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }
}
