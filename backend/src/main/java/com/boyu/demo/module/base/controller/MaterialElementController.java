package com.boyu.demo.module.base.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.base.entity.MaterialElement;
import com.boyu.demo.module.base.service.MaterialElementService;
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
 * 材质元素接口：GET/POST/PUT/DELETE /api/base/material-element。
 */
@RestController
@RequestMapping("/api/base/material-element")
public class MaterialElementController {

    private final MaterialElementService service;

    public MaterialElementController(MaterialElementService service) {
        this.service = service;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String symbol) {
        LambdaQueryWrapper<MaterialElement> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(symbol != null && !symbol.isBlank(), MaterialElement::getSymbol, symbol)
                .orderByAsc(MaterialElement::getSort);
        Page<MaterialElement> page = service.page(new Page<>(query.getPage(), query.getSize()), wrapper);
        return Result.ok(PageQuery.toPageMap(page));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('base:material-element:add')")
    public Result<Void> create(@RequestBody MaterialElement entity) {
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('base:material-element:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody MaterialElement entity) {
        entity.setId(id);
        service.updateById(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('base:material-element:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }
}
