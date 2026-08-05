package com.boyu.demo.module.flow.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.flow.entity.FlowTask;
import com.boyu.demo.module.flow.service.FlowTaskService;
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
 * 流程待办/已办接口：GET/POST/PUT/DELETE /api/flow/task。
 * <p>PUT 支持审批动作：status=DONE（结果 PASS）通过 / status=REJECTED（结果 REJECT）驳回，
 * 触发实例当前步骤自动推进；另提供 POST /{id}/approve 显式审批（body: result + remark）。
 */
@RestController
@RequestMapping("/api/flow/task")
public class FlowTaskController {

    private final FlowTaskService service;

    public FlowTaskController(FlowTaskService service) {
        this.service = service;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) Long instanceId,
                                            @RequestParam(required = false) String assignee,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<FlowTask> w = new LambdaQueryWrapper<>();
        w.eq(instanceId != null, FlowTask::getInstanceId, instanceId)
                .eq(assignee != null && !assignee.isBlank(), FlowTask::getAssignee, assignee)
                .eq(status != null && !status.isBlank(), FlowTask::getStatus, status)
                .and(keyword != null && !keyword.isBlank(),
                        wq -> wq.like(FlowTask::getStepName, keyword)
                                .or().like(FlowTask::getAssignee, keyword)
                                .or().like(FlowTask::getRemark, keyword))
                .orderByAsc(FlowTask::getId);
        return Result.ok(PageQuery.toPageMap(service.page(new Page<>(query.getPage(), query.getSize()), w)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('flow:task:add')")
    public Result<Void> create(@RequestBody FlowTask entity) {
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus("PENDING");
        }
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('flow:task:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody FlowTask entity) {
        entity.setId(id);
        String status = entity.getStatus();
        try {
            if ("DONE".equalsIgnoreCase(status)) {
                service.approve(id, "PASS", entity.getRemark());
            } else if ("REJECTED".equalsIgnoreCase(status)) {
                service.approve(id, "REJECT", entity.getRemark());
            } else {
                service.updateById(entity);
            }
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
        return Result.ok();
    }

    /**
     * 显式审批动作：body 传 {@code result}（PASS/REJECT）+ 可选 {@code remark}。
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('flow:task:update')")
    public Result<Void> approve(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String result = str(body, "result");
        String remark = str(body, "remark");
        try {
            service.approve(id, result, remark);
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('flow:task:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }

    private static String str(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return v == null ? null : String.valueOf(v);
    }
}
