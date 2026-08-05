package com.boyu.demo.module.finance.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.finance.entity.Expense;
import com.boyu.demo.module.finance.service.ExpenseService;
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
 * 费用管理接口：GET/POST/PUT/DELETE /api/finance/expense。
 * <p>分摊状态机：未分摊 UNALLOCATED → 已分摊 ALLOCATED（已分摊不可再分摊）。
 * PUT 分摊触发：body 仅含分摊状态（{@code {allocateStatus:'ALLOCATED'}}，前端分摊按钮）或
 * {@code action=allocate}；编辑表单提交完整实体走普通更新，分摊状态以库中为准、置 null 防越权。
 * 非法迁移（重复分摊）由 Service 抛 IllegalStateException，Controller 捕获转 Result.error 透传中文提示。
 */
@RestController
@RequestMapping("/api/finance/expense")
public class ExpenseController {

    /** 分摊流转允许出现的字段（其余字段出现视为普通编辑）。 */
    private static final Set<String> TRANSITION_KEYS = Set.of("id", "allocateStatus", "status", "action");

    private final ExpenseService service;
    private final ObjectMapper objectMapper;

    public ExpenseController(ExpenseService service, ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(required = false) String allocateStatus,
                                            @RequestParam(required = false) Integer marked,
                                            @RequestParam(required = false) String keyword) {
        String alloc = allocateStatus != null && !allocateStatus.isBlank() ? allocateStatus : status;
        LambdaQueryWrapper<Expense> w = new LambdaQueryWrapper<>();
        w.eq(alloc != null && !alloc.isBlank(), Expense::getAllocateStatus, alloc)
                .eq(marked != null, Expense::getMarked, marked)
                .and(keyword != null && !keyword.isBlank(),
                        wq -> wq.like(Expense::getExpenseNo, keyword)
                                .or().like(Expense::getProductName, keyword))
                .orderByDesc(Expense::getId);
        return Result.ok(PageQuery.toPageMap(service.page(new Page<>(query.getPage(), query.getSize()), w)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('finance:expense:add')")
    public Result<Void> create(@RequestBody Expense entity) {
        if (entity.getMarked() == null) {
            entity.setMarked(0);
        }
        if (entity.getAllocateStatus() == null || entity.getAllocateStatus().isBlank()) {
            entity.setAllocateStatus("UNALLOCATED");
        }
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:expense:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        // 路径 id 优先；body 未带 id 时用路径 id（兼容两种提交方式）
        Long realId = id != null ? id : longVal(body.get("id"));
        if (realId == null) {
            return Result.error("缺少费用单 id");
        }
        String action = str(body, "action");
        String allocStatus = str(body, "allocateStatus");
        if (allocStatus == null) {
            allocStatus = str(body, "status");
        }
        boolean allocateIntent = "allocate".equalsIgnoreCase(action)
                || ("ALLOCATED".equalsIgnoreCase(allocStatus) && onlyTransitionFields(body));
        if (allocateIntent) {
            try {
                service.allocate(realId);
                return Result.ok();
            } catch (IllegalStateException e) {
                return Result.error(e.getMessage());
            }
        }
        // 普通编辑（marked 标红等）：分摊状态只能走分摊动作，置 null 防止越权流转
        Expense entity = objectMapper.convertValue(body, Expense.class);
        entity.setId(realId);
        entity.setAllocateStatus(null);
        service.updateById(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:expense:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
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

    private static String str(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return v == null ? null : String.valueOf(v);
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
