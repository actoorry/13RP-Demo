package com.boyu.demo.module.base.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.base.entity.MobileConfig;
import com.boyu.demo.module.base.service.MobileConfigService;
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
 * 移动端主营品种配置接口：GET/POST/PUT/DELETE /api/base/mobile-config。
 */
@RestController
@RequestMapping("/api/base/mobile-config")
public class MobileConfigController {

    private final MobileConfigService service;

    public MobileConfigController(MobileConfigService service) {
        this.service = service;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String productName) {
        LambdaQueryWrapper<MobileConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(productName != null && !productName.isBlank(), MobileConfig::getProductName, productName)
                .orderByAsc(MobileConfig::getSort);
        Page<MobileConfig> page = service.page(new Page<>(query.getPage(), query.getSize()), wrapper);
        return Result.ok(PageQuery.toPageMap(page));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('base:mobile-config:add')")
    public Result<Void> create(@RequestBody MobileConfig entity) {
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('base:mobile-config:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody MobileConfig entity) {
        entity.setId(id);
        service.updateById(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('base:mobile-config:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }
}
