package com.boyu.demo.module.finance.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.finance.entity.Expense;
import com.boyu.demo.module.finance.mapper.ExpenseMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 费用管理服务。
 * <p>分摊状态机：未分摊 UNALLOCATED → 已分摊 ALLOCATED（不可逆，已分摊不可再分摊）。
 */
@Service
public class ExpenseService extends ServiceImpl<ExpenseMapper, Expense> {

    /** 分摊（仅未分摊 UNALLOCATED 的费用可分摊；已分摊拒绝且无副作用）。 */
    @Transactional
    public void allocate(Long id) {
        Expense expense = getById(id);
        if (expense == null) {
            throw new IllegalStateException("费用单不存在");
        }
        if ("ALLOCATED".equals(expense.getAllocateStatus())) {
            throw new IllegalStateException("该费用已分摊，不可重复分摊");
        }
        if (!"UNALLOCATED".equals(expense.getAllocateStatus())) {
            throw new IllegalStateException("仅未分摊(UNALLOCATED)的费用可分摊，当前：" + expense.getAllocateStatus());
        }
        expense.setAllocateStatus("ALLOCATED");
        updateById(expense);
    }
}
