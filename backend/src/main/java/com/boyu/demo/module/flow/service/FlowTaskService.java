package com.boyu.demo.module.flow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.flow.entity.AnmaInstance;
import com.boyu.demo.module.flow.entity.FlowTask;
import com.boyu.demo.module.flow.entity.X5Instance;
import com.boyu.demo.module.flow.mapper.AnmaInstanceMapper;
import com.boyu.demo.module.flow.mapper.FlowTaskMapper;
import com.boyu.demo.module.flow.mapper.X5InstanceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 流程待办/已办服务。
 * <p>审批动作联动：approve 接口传 task id + 结果（PASS/REJECT）。
 * PASS：任务置 DONE，实例当前步骤自动推进到下一个待办（无待办则实例 DONE）；
 * REJECT：任务置 DONE（已处理），实例 REJECTED。多办理人任一通过即推进实例状态。
 */
@Service
public class FlowTaskService extends ServiceImpl<FlowTaskMapper, FlowTask> {

    private final X5InstanceMapper x5Mapper;
    private final AnmaInstanceMapper anmaMapper;

    public FlowTaskService(X5InstanceMapper x5Mapper, AnmaInstanceMapper anmaMapper) {
        this.x5Mapper = x5Mapper;
        this.anmaMapper = anmaMapper;
    }

    /**
     * 审批动作：PASS 通过（推进实例当前步骤）/ REJECT 驳回（实例终止）。
     *
     * @param taskId 流程任务 id
     * @param result PASS / REJECT
     * @param remark 审批意见
     */
    @Transactional
    public void approve(Long taskId, String result, String remark) {
        FlowTask task = getById(taskId);
        if (task == null || !"PENDING".equals(task.getStatus())) {
            throw new IllegalStateException("任务不存在或已办理，不可重复审批");
        }
        if ("PASS".equalsIgnoreCase(result)) {
            task.setStatus("DONE");
            task.setRemark(remark);
            updateById(task);
            advanceInstance(task.getInstanceId());
        } else if ("REJECT".equalsIgnoreCase(result)) {
            task.setStatus("DONE");
            task.setRemark(remark == null ? "已驳回" : remark);
            updateById(task);
            rejectInstance(task.getInstanceId());
        } else {
            throw new IllegalStateException("不支持的审批结果：" + result);
        }
    }

    /** 实例审批通过时，把该实例所有 PENDING 待办置 DONE。 */
    @Transactional
    public void completeByInstance(Long instanceId) {
        LambdaQueryWrapper<FlowTask> w = new LambdaQueryWrapper<>();
        w.eq(FlowTask::getInstanceId, instanceId).eq(FlowTask::getStatus, "PENDING");
        List<FlowTask> tasks = list(w);
        for (FlowTask t : tasks) {
            t.setStatus("DONE");
        }
        updateBatchById(tasks);
    }

    /** 推进实例：当前待办通过后，将实例 current_step/approver 指向下一个待办；无待办则实例 DONE。 */
    private void advanceInstance(Long instanceId) {
        X5Instance x5 = x5Mapper.selectById(instanceId);
        if (x5 != null) {
            advanceX5(x5);
            return;
        }
        AnmaInstance anma = anmaMapper.selectById(instanceId);
        if (anma != null) {
            advanceAnma(anma);
        }
    }

    private void advanceX5(X5Instance inst) {
        if (!"RUNNING".equals(inst.getStatus())) {
            return;
        }
        FlowTask next = nextPendingTask(inst.getId());
        if (next == null) {
            inst.setStatus("DONE");
            inst.setCurrentStep("已完成");
        } else {
            inst.setCurrentStep(next.getStepName());
            inst.setApprover(next.getAssignee());
        }
        x5Mapper.updateById(inst);
    }

    private void advanceAnma(AnmaInstance inst) {
        if (!"RUNNING".equals(inst.getStatus())) {
            return;
        }
        FlowTask next = nextPendingTask(inst.getId());
        if (next == null) {
            inst.setStatus("DONE");
            inst.setCurrentStep("已完成");
        } else {
            inst.setCurrentStep(next.getStepName());
            inst.setApprover(next.getAssignee());
        }
        anmaMapper.updateById(inst);
    }

    /** 取该实例下一个未办理任务（按 id 升序，第一个 PENDING）。 */
    private FlowTask nextPendingTask(Long instanceId) {
        LambdaQueryWrapper<FlowTask> w = new LambdaQueryWrapper<>();
        w.eq(FlowTask::getInstanceId, instanceId)
                .eq(FlowTask::getStatus, "PENDING")
                .orderByAsc(FlowTask::getId);
        List<FlowTask> list = list(w);
        return list.isEmpty() ? null : list.get(0);
    }

    /** 驳回实例：仅 RUNNING 实例可驳回。 */
    private void rejectInstance(Long instanceId) {
        X5Instance x5 = x5Mapper.selectById(instanceId);
        if (x5 != null) {
            if ("RUNNING".equals(x5.getStatus())) {
                x5.setStatus("REJECTED");
                x5Mapper.updateById(x5);
            }
            return;
        }
        AnmaInstance anma = anmaMapper.selectById(instanceId);
        if (anma != null && "RUNNING".equals(anma.getStatus())) {
            anma.setStatus("REJECTED");
            anmaMapper.updateById(anma);
        }
    }
}
