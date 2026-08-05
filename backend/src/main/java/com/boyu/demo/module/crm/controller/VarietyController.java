package com.boyu.demo.module.crm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.crm.entity.Variety;
import com.boyu.demo.module.crm.service.VarietyService;
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
 * 品种资料接口：GET/POST/PUT/DELETE /api/crm/variety（月用量/下月计划/竞争对手/SWOT）。
 */
@RestController
@RequestMapping("/api/crm/variety")
public class VarietyController {

    private final VarietyService service;

    public VarietyController(VarietyService service) {
        this.service = service;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String varietyType,
                                            @RequestParam(required = false) String productName,
                                            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Variety> w = new LambdaQueryWrapper<>();
        w.eq(varietyType != null && !varietyType.isBlank(), Variety::getVarietyType, varietyType)
                .like(productName != null && !productName.isBlank(), Variety::getProductName, productName)
                .and(keyword != null && !keyword.isBlank(),
                        wq -> wq.like(Variety::getProductName, keyword)
                                .or().like(Variety::getGrade, keyword)
                                .or().like(Variety::getCompetitor, keyword))
                .orderByDesc(Variety::getId);
        return Result.ok(PageQuery.toPageMap(service.page(new Page<>(query.getPage(), query.getSize()), w)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('crm:variety:add')")
    public Result<Void> create(@RequestBody Variety entity) {
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('crm:variety:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Variety entity) {
        entity.setId(id);
        service.updateById(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('crm:variety:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }
}
