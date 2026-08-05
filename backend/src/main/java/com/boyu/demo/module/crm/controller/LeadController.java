package com.boyu.demo.module.crm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.crm.entity.Lead;
import com.boyu.demo.module.crm.service.LeadService;
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
 * 销售线索接口：GET/POST/PUT/DELETE /api/crm/lead（列表按 name 筛选）。
 */
@RestController
@RequestMapping("/api/crm/lead")
public class LeadController {

    private final LeadService service;

    public LeadController(LeadService service) {
        this.service = service;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String name,
                                            @RequestParam(required = false) String level,
                                            @RequestParam(required = false) Integer convertedFlag,
                                            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Lead> w = new LambdaQueryWrapper<>();
        w.like(name != null && !name.isBlank(), Lead::getName, name)
                .eq(level != null && !level.isBlank(), Lead::getLevel, level)
                .eq(convertedFlag != null, Lead::getConvertedFlag, convertedFlag)
                .and(keyword != null && !keyword.isBlank(),
                        wq -> wq.like(Lead::getName, keyword)
                                .or().like(Lead::getPhone, keyword))
                .orderByDesc(Lead::getId);
        return Result.ok(PageQuery.toPageMap(service.page(new Page<>(query.getPage(), query.getSize()), w)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('crm:lead:add')")
    public Result<Void> create(@RequestBody Lead entity) {
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('crm:lead:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Lead entity) {
        entity.setId(id);
        service.updateById(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('crm:lead:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }
}
