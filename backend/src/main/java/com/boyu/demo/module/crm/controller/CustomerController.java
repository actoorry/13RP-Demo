package com.boyu.demo.module.crm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.crm.entity.Customer;
import com.boyu.demo.module.crm.service.CustomerService;
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
 * 客户基本资料接口：GET/POST/PUT/DELETE /api/crm/customer（列表按 name 筛选）。
 */
@RestController
@RequestMapping("/api/crm/customer")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String name,
                                            @RequestParam(required = false) String level,
                                            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Customer> w = new LambdaQueryWrapper<>();
        w.like(name != null && !name.isBlank(), Customer::getName, name)
                .eq(level != null && !level.isBlank(), Customer::getLevel, level)
                .and(keyword != null && !keyword.isBlank(),
                        wq -> wq.like(Customer::getName, keyword)
                                .or().like(Customer::getPhone, keyword))
                .orderByDesc(Customer::getId);
        return Result.ok(PageQuery.toPageMap(service.page(new Page<>(query.getPage(), query.getSize()), w)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('crm:customer:add')")
    public Result<Void> create(@RequestBody Customer entity) {
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('crm:customer:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Customer entity) {
        entity.setId(id);
        service.updateById(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('crm:customer:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }
}
