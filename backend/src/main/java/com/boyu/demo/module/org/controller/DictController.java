package com.boyu.demo.module.org.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.org.entity.OrgDict;
import com.boyu.demo.module.org.service.OrgDictService;
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
 * 组织/岗位字典接口：GET/POST/PUT/DELETE /api/org/dict。
 */
@RestController
@RequestMapping("/api/org/dict")
public class DictController {

    private final OrgDictService service;

    public DictController(OrgDictService service) {
        this.service = service;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String dictType,
                                            @RequestParam(required = false) String name) {
        LambdaQueryWrapper<OrgDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dictType != null && !dictType.isBlank(), OrgDict::getDictType, dictType)
                .like(name != null && !name.isBlank(), OrgDict::getName, name)
                .orderByAsc(OrgDict::getSort);
        Page<OrgDict> page = service.page(new Page<>(query.getPage(), query.getSize()), wrapper);
        return Result.ok(PageQuery.toPageMap(page));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('org:dict:add')")
    public Result<Void> create(@RequestBody OrgDict entity) {
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('org:dict:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody OrgDict entity) {
        entity.setId(id);
        service.updateById(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('org:dict:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }
}
