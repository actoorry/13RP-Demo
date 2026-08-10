package com.boyu.demo.module.purchase.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.purchase.entity.Debt;
import com.boyu.demo.module.purchase.service.DebtService;
import org.springframework.security.access.prepost.PreAuthorize;
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
 * 进项欠票接口：GET/POST/PUT /api/purchase/debt（一入库单一欠票；一键生成/新增+关联）。
 */
@RestController
@RequestMapping("/api/purchase/debt")
public class DebtController {

    private final DebtService service;

    public DebtController(DebtService service) {
        this.service = service;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Debt> w = new LambdaQueryWrapper<>();
        w.eq(status != null && !status.isBlank(), Debt::getStatus, status)
                .and(keyword != null && !keyword.isBlank(),
                        wq -> wq.like(Debt::getInboundNo, keyword)
                                .or().like(Debt::getInvoiceNo, keyword)
                                .or().like(Debt::getSupplierName, keyword))
                .orderByDesc(Debt::getId);
        return Result.ok(PageQuery.toPageMap(service.page(new Page<>(query.getPage(), query.getSize()), w)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('purchase:debt:add')")
    public Result<Void> create(@RequestBody Debt entity) {
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus("OPEN");
        }
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('purchase:debt:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Debt entity) {
        entity.setId(id);
        service.updateById(entity);
        return Result.ok();
    }
}
