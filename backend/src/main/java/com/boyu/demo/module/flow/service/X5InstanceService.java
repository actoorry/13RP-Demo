package com.boyu.demo.module.flow.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.flow.entity.FlowTask;
import com.boyu.demo.module.flow.entity.X5Instance;
import com.boyu.demo.module.flow.mapper.X5InstanceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * X5 流程实例服务：500 元分级审批（金额 &lt;500 单级，≥500 多级），状态 RUNNING → DONE / REJECTED。
 */
@Service
public class X5InstanceService extends ServiceImpl<X5InstanceMapper, X5Instance> {

    private static final BigDecimal LEVEL_500 = new BigDecimal("500");

    private final FlowTaskService flowTaskService;

    public X5InstanceService(FlowTaskService flowTaskService) {
        this.flowTaskService = flowTaskService;
    }

    /** 创建实例并按金额自动生成审批任务（500 元分级审批）。 */
    @Transactional
    public void createInstance(X5Instance entity) {
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus("RUNNING");
        }
        save(entity);
        BigDecimal amount = entity.getAmount() == null ? BigDecimal.ZERO : entity.getAmount();
        List<FlowTask> tasks = new ArrayList<>();
        if (amount.compareTo(LEVEL_500) < 0) {
            tasks.add(buildTask(entity.getId(), "直接审批", entity.getApprover()));
        } else {
            tasks.add(buildTask(entity.getId(), "部门审批", entity.getApprover()));
            tasks.add(buildTask(entity.getId(), "财务审批", null));
        }
        flowTaskService.saveBatch(tasks);
    }

    private FlowTask buildTask(Long instanceId, String stepName, String assignee) {
        FlowTask t = new FlowTask();
        t.setInstanceId(instanceId);
        t.setStepName(stepName);
        t.setAssignee(assignee);
        t.setStatus("PENDING");
        return t;
    }

    /** 审批通过（仅 RUNNING 可审批）：实例 DONE，对应待办全部置已办。 */
    @Transactional
    public void approve(Long id) {
        X5Instance inst = getById(id);
        if (inst == null || !"RUNNING".equals(inst.getStatus())) {
            throw new IllegalStateException("流程不存在或已结束，不可审批");
        }
        inst.setStatus("DONE");
        inst.setCurrentStep("已完成");
        updateById(inst);
        flowTaskService.completeByInstance(id);
    }

    /** 驳回（仅 RUNNING 可驳回）：实例 REJECTED。 */
    @Transactional
    public void reject(Long id) {
        X5Instance inst = getById(id);
        if (inst == null || !"RUNNING".equals(inst.getStatus())) {
            throw new IllegalStateException("流程不存在或已结束，不可驳回");
        }
        inst.setStatus("REJECTED");
        updateById(inst);
    }
}
