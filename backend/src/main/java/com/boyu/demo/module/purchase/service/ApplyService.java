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
 * 记录不存在 / 状态非法时抛 IllegalStateException（Controller 捕获转 Result.error）。
 */
@Service
public class ApplyService extends ServiceImpl<ApplyMapper, Apply> {

    /** 第一段审批：批准（仅待批准 PENDING_APPROVE 的申请可批准）。 */
    @Transactional
    public void approve(Long id) {
        Apply apply = require(id);
        if (!"PENDING_APPROVE".equals(apply.getStatus())) {
            throw new IllegalStateException("仅待批准(PENDING_APPROVE)状态的采购申请单可批准，当前：" + apply.getStatus());
        }
        apply.setStatus("APPROVED");
        apply.setApprover(SecurityUtils.currentAccount());
        apply.setApproveTime(LocalDateTime.now());
        updateById(apply);
    }

    /** 进入待复核（仅已批准 APPROVED 的申请可进入待复核）。 */
    @Transactional
    public void toReview(Long id) {
        Apply apply = require(id);
        if (!"APPROVED".equals(apply.getStatus())) {
            throw new IllegalStateException("仅已批准(APPROVED)状态的采购申请单可进入待复核，当前：" + apply.getStatus());
        }
        apply.setStatus("PENDING_REVIEW");
        updateById(apply);
    }

    /** 第二段审批：客服部复核（仅待复核 PENDING_REVIEW 的申请可复核；复核通过后自动生成待审批订单）。 */
    @Transactional
    public void review(Long id) {
        Apply apply = require(id);
        if (!"PENDING_REVIEW".equals(apply.getStatus())) {
            throw new IllegalStateException("仅待复核(PENDING_REVIEW)状态的采购申请单可复核，当前：" + apply.getStatus());
        }
        apply.setStatus("REVIEWED");
        apply.setReviewer(SecurityUtils.currentAccount());
        apply.setReviewTime(LocalDateTime.now());
        updateById(apply);
    }

    private Apply require(Long id) {
        Apply apply = getById(id);
        if (apply == null) {
            throw new IllegalStateException("采购申请单不存在：id=" + id);
        }
        return apply;
    }
}
