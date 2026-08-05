package com.boyu.demo.module.base.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.base.entity.PackageStandard;
import com.boyu.demo.module.base.service.PackageStandardService;
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
 * 合同包装验收标准接口：GET/POST/PUT/DELETE /api/base/package-standard。
 */
@RestController
@RequestMapping("/api/base/package-standard")
public class PackageStandardController {

    private final PackageStandardService service;

    public PackageStandardController(PackageStandardService service) {
        this.service = service;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String packageName) {
        LambdaQueryWrapper<PackageStandard> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(packageName != null && !packageName.isBlank(), PackageStandard::getPackageName, packageName)
                .orderByAsc(PackageStandard::getId);
        Page<PackageStandard> page = service.page(new Page<>(query.getPage(), query.getSize()), wrapper);
        return Result.ok(PageQuery.toPageMap(page));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('base:package-standard:add')")
    public Result<Void> create(@RequestBody PackageStandard entity) {
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('base:package-standard:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody PackageStandard entity) {
        entity.setId(id);
        service.updateById(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('base:package-standard:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }
}
