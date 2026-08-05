package com.boyu.demo.module.flow.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.flow.entity.AnmaInstance;
import com.boyu.demo.module.flow.service.AnmaInstanceService;
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
 * 安码流程实例接口：GET/POST/PUT/DELETE /api/flow/anma。
 * <p>PUT 支持状态流转 status=DONE 通过 / status=REJECTED 驳回。
 */
@RestController
@RequestMapping("/api/flow/anma")
public class AnmaInstanceController {

    private final AnmaInstanceService service;

    public AnmaInstanceController(AnmaInstanceService service) {
        this.service = service;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String flowType,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<AnmaInstance> w = new LambdaQueryWrapper<>();
        w.eq(flowType != null && !flowType.isBlank(), AnmaInstance::getFlowType, flowType)
                .eq(status != null && !status.isBlank(), AnmaInstance::getStatus, status)
                .and(keyword != null && !keyword.isBlank(),
                        wq -> wq.like(AnmaInstance::getFlowNo, keyword)
                                .or().like(AnmaInstance::getTitle, keyword)
                                .or().like(AnmaInstance::getFlowType, keyword))
                .orderByDesc(AnmaInstance::getId);
        return Result.ok(PageQuery.toPageMap(service.pageWithNames(new Page<>(query.getPage(), query.getSize()), w)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('flow:anma:add')")
    public Result<Void> create(@RequestBody AnmaInstance entity) {
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus("RUNNING");
        }
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('flow:anma:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody AnmaInstance entity) {
        entity.setId(id);
        String status = entity.getStatus();
        try {
            if ("DONE".equalsIgnoreCase(status)) {
                service.approve(id);
            } else if ("REJECTED".equalsIgnoreCase(status)) {
                service.reject(id);
            } else {
                service.updateById(entity);
            }
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('flow:anma:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }
}
