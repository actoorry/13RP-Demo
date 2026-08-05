package com.boyu.demo.module.base.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.base.entity.Product;
import com.boyu.demo.module.base.service.ProductService;
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

import java.util.List;
import java.util.Map;

/**
 * 产品主数据接口：GET/POST/PUT/DELETE /api/base/product；
 * GET /api/base/product/tree?accountId= 返回品名→牌号→材质树（按账套隔离）。
 * <p>DELETE 为作废（级联作废下级数据）。
 */
@RestController
@RequestMapping("/api/base/product")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) Long accountId,
                                            @RequestParam(required = false) String name,
                                            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(accountId != null, Product::getAccountId, accountId)
                .like(name != null && !name.isBlank(), Product::getName, name)
                .eq(status != null, Product::getStatus, status)
                .orderByAsc(Product::getSort);
        Page<Product> page = service.page(new Page<>(query.getPage(), query.getSize()), wrapper);
        return Result.ok(PageQuery.toPageMap(page));
    }

    /** 产品树：品名→牌号→材质元素（按账套隔离）。 */
    @GetMapping("/tree")
    public Result<List<Map<String, Object>>> tree(@RequestParam Long accountId) {
        return Result.ok(service.buildTree(accountId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('base:product:add')")
    public Result<Void> create(@RequestBody Product entity) {
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('base:product:edit')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Product entity) {
        entity.setId(id);
        service.updateById(entity);
        return Result.ok();
    }

    /** 作废产品（级联提示：下级数据都会被作废）。 */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('base:product:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        service.invalidate(id);
        return Result.ok();
    }
}
