package com.boyu.demo.module.inventory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.inventory.entity.InventorySafeStock;
import com.boyu.demo.module.inventory.service.InventorySafeStockService;
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
 * 安全库存设计接口：GET/POST/PUT/DELETE /api/inventory/safe-stock（按 product_name 查）。
 */
@RestController
@RequestMapping("/api/inventory/safe-stock")
public class SafeStockController {

    private final InventorySafeStockService service;

    public SafeStockController(InventorySafeStockService service) {
        this.service = service;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String productName,
                                            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<InventorySafeStock> w = new LambdaQueryWrapper<>();
        w.like(productName != null && !productName.isBlank(), InventorySafeStock::getProductName, productName)
                .and(keyword != null && !keyword.isBlank(),
                        wq -> wq.like(InventorySafeStock::getProductName, keyword)
                                .or().like(InventorySafeStock::getMaterial, keyword))
                .orderByDesc(InventorySafeStock::getId);
        return Result.ok(PageQuery.toPageMap(service.page(new Page<>(query.getPage(), query.getSize()), w)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('inventory:safe-stock:add')")
    public Result<Void> create(@RequestBody InventorySafeStock entity) {
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:safe-stock:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody InventorySafeStock entity) {
        entity.setId(id);
        service.updateById(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:safe-stock:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }
}
