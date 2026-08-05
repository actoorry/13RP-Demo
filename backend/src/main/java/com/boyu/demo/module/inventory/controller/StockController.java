package com.boyu.demo.module.inventory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.inventory.entity.InventoryStock;
import com.boyu.demo.module.inventory.service.InventoryStockService;
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
 * 库存统计接口：GET/POST/PUT/DELETE /api/inventory/stock。
 * <p>列表每条记录额外返回 {@code warn} 字段：库龄 stock_age ≥ age_warn_days 时为 true（红色预警）。
 */
@RestController
@RequestMapping("/api/inventory/stock")
public class StockController {

    private final InventoryStockService service;

    public StockController(InventoryStockService service) {
        this.service = service;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String productName,
                                            @RequestParam(required = false) Long orgId,
                                            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<InventoryStock> w = new LambdaQueryWrapper<>();
        w.like(productName != null && !productName.isBlank(), InventoryStock::getProductName, productName)
                .eq(orgId != null, InventoryStock::getOrgId, orgId)
                .and(keyword != null && !keyword.isBlank(),
                        wq -> wq.like(InventoryStock::getProductName, keyword)
                                .or().like(InventoryStock::getGrade, keyword)
                                .or().like(InventoryStock::getSpec, keyword))
                .orderByDesc(InventoryStock::getId);
        Page<InventoryStock> page = service.page(new Page<>(query.getPage(), query.getSize()), w);
        // 库龄预警：stock_age ≥ age_warn_days → warn=true
        for (InventoryStock s : page.getRecords()) {
            s.setWarn(s.getStockAge() != null && s.getAgeWarnDays() != null
                    && s.getStockAge() >= s.getAgeWarnDays());
        }
        return Result.ok(PageQuery.toPageMap(page));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('inventory:stock:add')")
    public Result<Void> create(@RequestBody InventoryStock entity) {
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:stock:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody InventoryStock entity) {
        entity.setId(id);
        service.updateById(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:stock:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }
}
