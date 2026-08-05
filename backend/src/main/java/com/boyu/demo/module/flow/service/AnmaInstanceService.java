package com.boyu.demo.module.flow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.crm.entity.Customer;
import com.boyu.demo.module.crm.mapper.CustomerMapper;
import com.boyu.demo.module.flow.entity.AnmaInstance;
import com.boyu.demo.module.flow.mapper.AnmaInstanceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 安码流程实例服务：合同/财务审批，状态 RUNNING → DONE / REJECTED。
 */
@Service
public class AnmaInstanceService extends ServiceImpl<AnmaInstanceMapper, AnmaInstance> {

    private final FlowTaskService flowTaskService;
    private final CustomerMapper customerMapper;

    public AnmaInstanceService(FlowTaskService flowTaskService, CustomerMapper customerMapper) {
        this.flowTaskService = flowTaskService;
        this.customerMapper = customerMapper;
    }

    /** 分页查询并填充供应商/客户名称（供应商与客户均取 crm_customer 表）。 */
    public IPage<AnmaInstance> pageWithNames(Page<AnmaInstance> page, LambdaQueryWrapper<AnmaInstance> w) {
        IPage<AnmaInstance> result = page(page, w);
        if (result.getRecords().isEmpty()) {
            return result;
        }
        Set<Long> ids = new HashSet<>();
        for (AnmaInstance inst : result.getRecords()) {
            if (inst.getSupplierId() != null) {
                ids.add(inst.getSupplierId());
            }
            if (inst.getCustomerId() != null) {
                ids.add(inst.getCustomerId());
            }
        }
        if (ids.isEmpty()) {
            return result;
        }
        Map<Long, String> nameMap = new HashMap<>();
        customerMapper.selectBatchIds(ids).forEach(c -> nameMap.put(c.getId(), c.getName()));
        for (AnmaInstance inst : result.getRecords()) {
            inst.setSupplierName(inst.getSupplierId() == null ? null : nameMap.get(inst.getSupplierId()));
            inst.setCustomerName(inst.getCustomerId() == null ? null : nameMap.get(inst.getCustomerId()));
        }
        return result;
    }

    /** 审批通过（仅 RUNNING 可审批）：实例 DONE，对应待办全部置已办。 */
    @Transactional
    public void approve(Long id) {
        AnmaInstance inst = getById(id);
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
        AnmaInstance inst = getById(id);
        if (inst == null || !"RUNNING".equals(inst.getStatus())) {
            throw new IllegalStateException("流程不存在或已结束，不可驳回");
        }
        inst.setStatus("REJECTED");
        updateById(inst);
    }
}
