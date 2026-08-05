package com.boyu.demo.module.finance.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.finance.entity.Arrival;
import com.boyu.demo.module.finance.service.ArrivalService;
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
 * 到账公告接口：GET/POST/PUT/DELETE /api/finance/arrival。
 */
@RestController
@RequestMapping("/api/finance/arrival")
public class ArrivalController {

    private final ArrivalService service;

    public ArrivalController(ArrivalService service) {
        this.service = service;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) Long accountId,
                                            @RequestParam(required = false) Long orgId,
                                            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Arrival> w = new LambdaQueryWrapper<>();
        w.eq(accountId != null, Arrival::getAccountId, accountId)
                .eq(orgId != null, Arrival::getOrgId, orgId)
                .and(keyword != null && !keyword.isBlank(),
                        wq -> wq.like(Arrival::getOperator, keyword))
                .orderByDesc(Arrival::getId);
        return Result.ok(PageQuery.toPageMap(service.page(new Page<>(query.getPage(), query.getSize()), w)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('finance:arrival:add')")
    public Result<Void> create(@RequestBody Arrival entity) {
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:arrival:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Arrival entity) {
        entity.setId(id);
        service.updateById(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:arrival:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }
}
