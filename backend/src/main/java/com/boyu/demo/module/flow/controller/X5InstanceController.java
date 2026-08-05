package com.boyu.demo.module.flow.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.flow.entity.X5Instance;
import com.boyu.demo.module.flow.service.X5InstanceService;
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
 * X5 流程实例接口：GET/POST/PUT/DELETE /api/flow/x5。
 * <p>POST 按金额自动生成审批任务（500 元分级审批）；PUT 支持状态流转 status=DONE 通过 / status=REJECTED 驳回。
 */
@RestController
@RequestMapping("/api/flow/x5")
public class X5InstanceController {

    private final X5InstanceService service;

    public X5InstanceController(X5InstanceService service) {
        this.service = service;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String flowType,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(required = false) String applicant,
                                            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<X5Instance> w = new LambdaQueryWrapper<>();
        w.eq(flowType != null && !flowType.isBlank(), X5Instance::getFlowType, flowType)
                .eq(status != null && !status.isBlank(), X5Instance::getStatus, status)
                .eq(applicant != null && !applicant.isBlank(), X5Instance::getApplicant, applicant)
                .and(keyword != null && !keyword.isBlank(),
                        wq -> wq.like(X5Instance::getFlowNo, keyword)
                                .or().like(X5Instance::getTitle, keyword)
                                .or().like(X5Instance::getFlowType, keyword)
                                .or().like(X5Instance::getApplicant, keyword))
                .orderByDesc(X5Instance::getId);
        return Result.ok(PageQuery.toPageMap(service.page(new Page<>(query.getPage(), query.getSize()), w)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('flow:x5:add')")
    public Result<Void> create(@RequestBody X5Instance entity) {
        service.createInstance(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('flow:x5:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody X5Instance entity) {
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
    @PreAuthorize("hasAuthority('flow:x5:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }
}
