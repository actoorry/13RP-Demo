package com.boyu.demo.module.purchase.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.purchase.entity.Payable;
import com.boyu.demo.module.purchase.service.PayableService;
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
 * 应付列表接口：GET/POST/PUT /api/purchase/payable（匹配应付余额直接生成付款单）。
 */
@RestController
@RequestMapping("/api/purchase/payable")
public class PayableController {

    private final PayableService service;

    public PayableController(PayableService service) {
        this.service = service;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<Payable> w = new LambdaQueryWrapper<>();
        w.eq(status != null && !status.isBlank(), Payable::getStatus, status)
                .orderByAsc(Payable::getDueDate);
        return Result.ok(PageQuery.toPageMap(service.page(new Page<>(query.getPage(), query.getSize()), w)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('purchase:payable:add')")
    public Result<Void> create(@RequestBody Payable entity) {
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus("OPEN");
        }
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('purchase:payable:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Payable entity) {
        entity.setId(id);
        service.updateById(entity);
        return Result.ok();
    }
}
