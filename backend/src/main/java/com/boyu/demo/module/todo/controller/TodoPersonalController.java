package com.boyu.demo.module.todo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.todo.entity.TodoPersonal;
import com.boyu.demo.module.todo.service.TodoPersonalService;
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
 * 个人待办接口：GET/POST/PUT/DELETE /api/todo/personal。
 * <p>PUT 支持状态流转 status=DONE 完成（仅待办 PENDING 可完成）。
 */
@RestController
@RequestMapping("/api/todo/personal")
public class TodoPersonalController {

    private final TodoPersonalService service;

    public TodoPersonalController(TodoPersonalService service) {
        this.service = service;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String todoType,
                                            @RequestParam(required = false) String templateType,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<TodoPersonal> w = new LambdaQueryWrapper<>();
        w.eq(todoType != null && !todoType.isBlank(), TodoPersonal::getTodoType, todoType)
                .eq(templateType != null && !templateType.isBlank(), TodoPersonal::getTemplateType, templateType)
                .eq(status != null && !status.isBlank(), TodoPersonal::getStatus, status)
                .and(keyword != null && !keyword.isBlank(),
                        wq -> wq.like(TodoPersonal::getTodoType, keyword)
                                .or().like(TodoPersonal::getTemplateType, keyword)
                                .or().like(TodoPersonal::getAssignee, keyword))
                .orderByAsc(TodoPersonal::getId);
        return Result.ok(PageQuery.toPageMap(service.page(new Page<>(query.getPage(), query.getSize()), w)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('todo:personal:add')")
    public Result<Void> create(@RequestBody TodoPersonal entity) {
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus("PENDING");
        }
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('todo:personal:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody TodoPersonal entity) {
        entity.setId(id);
        String status = entity.getStatus();
        try {
            if ("DONE".equalsIgnoreCase(status)) {
                service.done(id);
            } else {
                service.updateById(entity);
            }
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('todo:personal:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }
}
