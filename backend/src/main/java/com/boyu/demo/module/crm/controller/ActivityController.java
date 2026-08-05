package com.boyu.demo.module.crm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.crm.entity.Activity;
import com.boyu.demo.module.crm.service.ActivityService;
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
 * 活动管理接口：GET/POST/PUT/DELETE /api/crm/activity（使用/生产/经营，主客/次客等关系）。
 */
@RestController
@RequestMapping("/api/crm/activity")
public class ActivityController {

    private final ActivityService service;

    public ActivityController(ActivityService service) {
        this.service = service;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String activityType,
                                            @RequestParam(required = false) String relation,
                                            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Activity> w = new LambdaQueryWrapper<>();
        w.eq(activityType != null && !activityType.isBlank(), Activity::getActivityType, activityType)
                .eq(relation != null && !relation.isBlank(), Activity::getRelation, relation)
                .and(keyword != null && !keyword.isBlank(),
                        wq -> wq.like(Activity::getProductName, keyword)
                                .or().like(Activity::getContent, keyword)
                                .or().like(Activity::getCreator, keyword))
                .orderByDesc(Activity::getId);
        return Result.ok(PageQuery.toPageMap(service.page(new Page<>(query.getPage(), query.getSize()), w)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('crm:activity:add')")
    public Result<Void> create(@RequestBody Activity entity) {
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('crm:activity:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Activity entity) {
        entity.setId(id);
        service.updateById(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('crm:activity:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }
}
