package com.boyu.demo.module.flow.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.flow.entity.FlowTask;
import com.boyu.demo.module.flow.service.FlowTaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Set;

/**
 * 流程待办/已办接口：GET/POST/PUT/DELETE /api/flow/task。
 * <p>PUT 完成流转：body 仅含流转字段（{@code {status:'DONE'}} 或 {@code {status:'REJECTED'}}，前端办理按钮）时
 * 调用 service.approve（PASS/REJECT），触发实例当前步骤自动推进；编辑表单提交完整实体走普通更新，
 * 状态以库中为准、置 null 防越权（状态只能走流转动作）。另提供 POST /{id}/approve 显式审批（body: result + remark）。
 */
@RestController
@RequestMapping("/api/flow/task")
public class FlowTaskController {

    /** 流转动作允许出现的字段（其余字段出现视为普通编辑）。 */
    private static final Set<String> TRANSITION_KEYS = Set.of("id", "status", "remark");

    private final FlowTaskService service;
    private final ObjectMapper objectMapper;

    public FlowTaskController(FlowTaskService service, ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
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
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long realId = id != null ? id : longVal(body.get("id"));
        if (realId == null) {
            return Result.error("缺少任务 id");
        }
        String status = str(body, "status");
        String remark = str(body, "remark");
        boolean transitionIntent = ("DONE".equalsIgnoreCase(status) || "REJECTED".equalsIgnoreCase(status))
                && onlyTransitionFields(body);
        if (transitionIntent) {
            try {
                service.approve(realId, "DONE".equalsIgnoreCase(status) ? "PASS" : "REJECT", remark);
                return Result.ok();
            } catch (IllegalStateException e) {
                return Result.error(e.getMessage());
            }
        }
        // 普通编辑（步骤名/办理人/备注等）：状态只能走上方流转动作，置 null 防止越权流转
        FlowTask entity = objectMapper.convertValue(body, FlowTask.class);
        entity.setId(realId);
        entity.setStatus(null);
        service.updateById(entity);
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

    private static boolean onlyTransitionFields(Map<String, Object> body) {
        for (String k : body.keySet()) {
            if (TRANSITION_KEYS.contains(k)) {
                continue;
            }
            return false;
        }
        return true;
    }

    private static Long longVal(Object v) {
        if (v instanceof Number n) {
            return n.longValue();
        }
        if (v instanceof String s) {
            try {
                return Long.parseLong(s.trim());
            } catch (NumberFormatException ignore) {
                return null;
            }
        }
        return null;
    }
}
