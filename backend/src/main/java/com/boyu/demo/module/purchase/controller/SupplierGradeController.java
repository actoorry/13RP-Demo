package com.boyu.demo.module.purchase.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.purchase.entity.SupplierGrade;
import com.boyu.demo.module.purchase.service.SupplierGradeService;
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
 * 供应商分级接口：GET/POST/PUT/DELETE /api/purchase/supplier-grade。
 */
@RestController
@RequestMapping("/api/purchase/supplier-grade")
public class SupplierGradeController {

    private final SupplierGradeService service;

    public SupplierGradeController(SupplierGradeService service) {
        this.service = service;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String grade,
                                            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<SupplierGrade> w = new LambdaQueryWrapper<>();
        w.eq(grade != null && !grade.isBlank(), SupplierGrade::getGrade, grade)
                .and(keyword != null && !keyword.isBlank(),
                        wq -> wq.like(SupplierGrade::getSupplierName, keyword)
                                .or().like(SupplierGrade::getGrade, keyword))
                .orderByAsc(SupplierGrade::getId);
        return Result.ok(PageQuery.toPageMap(service.page(new Page<>(query.getPage(), query.getSize()), w)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('purchase:supplier-grade:add')")
    public Result<Void> create(@RequestBody SupplierGrade entity) {
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('purchase:supplier-grade:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody SupplierGrade entity) {
        entity.setId(id);
        service.updateById(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('purchase:supplier-grade:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }
}
