package com.boyu.demo.module.purchase.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.common.SecurityUtils;
import com.boyu.demo.module.purchase.entity.Apply;
import com.boyu.demo.module.purchase.mapper.ApplyMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 采购申请审批链服务：批准→复核两段审批。
 * <p>状态机：待批准 PENDING_APPROVE → 已批准 APPROVED → 待复核 PENDING_REVIEW → 已复核 REVIEWED。
 */
@Service
public class ApplyService extends ServiceImpl<ApplyMapper, Apply> {

    /** 第一段审批：批准。 */
    @Transactional
    public void approve(Long id) {
        Apply apply = getById(id);
        if (apply == null || !"PENDING_APPROVE".equals(apply.getStatus())) {
            return;
        }
        apply.setStatus("APPROVED");
        apply.setApprover(SecurityUtils.currentAccount());
        apply.setApproveTime(LocalDateTime.now());
        updateById(apply);
    }

    /** 进入待复核（仅已批准 APPROVED 的申请可进入待复核）。 */
    @Transactional
    public void toReview(Long id) {
        Apply apply = getById(id);
        if (apply == null || !"APPROVED".equals(apply.getStatus())) {
            return;
        }
        apply.setStatus("PENDING_REVIEW");
        updateById(apply);
    }

    /** 第二段审批：客服部复核（复核通过后自动生成待审批订单）。 */
    @Transactional
    public void review(Long id) {
        Apply apply = getById(id);
        if (apply == null || !"PENDING_REVIEW".equals(apply.getStatus())) {
            return;
        }
        apply.setStatus("REVIEWED");
        apply.setReviewer(SecurityUtils.currentAccount());
        apply.setReviewTime(LocalDateTime.now());
        updateById(apply);
    }
}
