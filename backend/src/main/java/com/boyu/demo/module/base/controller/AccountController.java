package com.boyu.demo.module.base.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.base.entity.Account;
import com.boyu.demo.module.base.service.AccountService;
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
 * 账套管理接口：GET/POST/PUT/DELETE /api/base/account。
 */
@RestController
@RequestMapping("/api/base/account")
public class AccountController {

    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String name,
                                            @RequestParam(required = false) Integer status,
                                            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Account> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(name != null && !name.isBlank(), Account::getName, name)
                .eq(status != null, Account::getStatus, status)
                .and(keyword != null && !keyword.isBlank(),
                        wq -> wq.like(Account::getName, keyword)
                                .or().like(Account::getCode, keyword))
                .orderByAsc(Account::getId);
        Page<Account> page = service.page(new Page<>(query.getPage(), query.getSize()), wrapper);
        return Result.ok(PageQuery.toPageMap(page));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('base:account:add')")
    public Result<Void> create(@RequestBody Account entity) {
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('base:account:edit')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Account entity) {
        entity.setId(id);
        service.updateById(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('base:account:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }
}
