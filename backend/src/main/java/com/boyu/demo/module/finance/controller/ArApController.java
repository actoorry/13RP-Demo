package com.boyu.demo.module.finance.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.finance.entity.ArAp;
import com.boyu.demo.module.finance.service.ArApService;
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
 * 应收应付接口：GET/POST/PUT/DELETE /api/finance/ar-ap。
 */
@RestController
@RequestMapping("/api/finance/ar-ap")
public class ArApController {

    private final ArApService service;

    public ArApController(ArApService service) {
        this.service = service;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String partyType,
                                            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<ArAp> w = new LambdaQueryWrapper<>();
        w.eq(partyType != null && !partyType.isBlank(), ArAp::getPartyType, partyType)
                .and(keyword != null && !keyword.isBlank(),
                        wq -> wq.like(ArAp::getPartyId, keyword))
                .orderByDesc(ArAp::getId);
        return Result.ok(PageQuery.toPageMap(service.page(new Page<>(query.getPage(), query.getSize()), w)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('finance:ar-ap:add')")
    public Result<Void> create(@RequestBody ArAp entity) {
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:ar-ap:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody ArAp entity) {
        entity.setId(id);
        service.updateById(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:ar-ap:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }
}
