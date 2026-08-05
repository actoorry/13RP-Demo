package com.boyu.demo.module.todo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.todo.entity.TodoSubscription;
import com.boyu.demo.module.todo.service.TodoSubscriptionService;
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
 * 四板块订阅接口：GET/POST/PUT/DELETE /api/todo/subscription。
 * <p>config_json 为 JSON 字符串（存阀值）；enabled 启用/停用。
 */
@RestController
@RequestMapping("/api/todo/subscription")
public class TodoSubscriptionController {

    private final TodoSubscriptionService service;

    public TodoSubscriptionController(TodoSubscriptionService service) {
        this.service = service;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String boardType,
                                            @RequestParam(required = false) String subType,
                                            @RequestParam(required = false) Integer enabled,
                                            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<TodoSubscription> w = new LambdaQueryWrapper<>();
        w.eq(boardType != null && !boardType.isBlank(), TodoSubscription::getBoardType, boardType)
                .eq(subType != null && !subType.isBlank(), TodoSubscription::getSubType, subType)
                .eq(enabled != null, TodoSubscription::getEnabled, enabled)
                .and(keyword != null && !keyword.isBlank(),
                        wq -> wq.like(TodoSubscription::getBoardType, keyword)
                                .or().like(TodoSubscription::getSubType, keyword))
                .orderByAsc(TodoSubscription::getId);
        return Result.ok(PageQuery.toPageMap(service.page(new Page<>(query.getPage(), query.getSize()), w)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('todo:subscription:add')")
    public Result<Void> create(@RequestBody TodoSubscription entity) {
        if (entity.getEnabled() == null) {
            entity.setEnabled(1);
        }
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('todo:subscription:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody TodoSubscription entity) {
        entity.setId(id);
        service.updateById(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('todo:subscription:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }
}
