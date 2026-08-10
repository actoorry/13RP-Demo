package com.boyu.demo.module.purchase.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.purchase.entity.Forecast;
import com.boyu.demo.module.purchase.service.ForecastService;
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
 * 预测预案接口：GET/POST/PUT/DELETE /api/purchase/forecast（年/月/周/日）。
 */
@RestController
@RequestMapping("/api/purchase/forecast")
public class ForecastController {

    private final ForecastService service;

    public ForecastController(ForecastService service) {
        this.service = service;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String planType,
                                            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Forecast> w = new LambdaQueryWrapper<>();
        w.eq(planType != null && !planType.isBlank(), Forecast::getPlanType, planType)
                .and(keyword != null && !keyword.isBlank(),
                        wq -> wq.like(Forecast::getPlanType, keyword)
                                .or().like(Forecast::getPlanName, keyword))
                .orderByDesc(Forecast::getId);
        return Result.ok(PageQuery.toPageMap(service.page(new Page<>(query.getPage(), query.getSize()), w)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('purchase:forecast:add')")
    public Result<Void> create(@RequestBody Forecast entity) {
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('purchase:forecast:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Forecast entity) {
        entity.setId(id);
        service.updateById(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('purchase:forecast:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }
}
