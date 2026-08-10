package com.boyu.demo.module.purchase.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.common.SecurityUtils;
import com.boyu.demo.module.purchase.entity.Apply;
import com.boyu.demo.module.purchase.entity.Inquiry;
import com.boyu.demo.module.purchase.entity.PurchaseOrder;
import com.boyu.demo.module.purchase.mapper.ApplyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 采购申请审批链服务：批准→复核两段审批。
 * <p>状态机：待批准 PENDING_APPROVE → 已批准 APPROVED → 待复核 PENDING_REVIEW → 已复核 REVIEWED。
 * 记录不存在 / 状态非法时抛 IllegalStateException（Controller 捕获转 Result.error）。
 */
@Service
public class ApplyService extends ServiceImpl<ApplyMapper, Apply> {

    private static final Logger log = LoggerFactory.getLogger(ApplyService.class);

    private final InquiryService inquiryService;
    private final OrderService orderService;

    public ApplyService(InquiryService inquiryService, OrderService orderService) {
        this.inquiryService = inquiryService;
        this.orderService = orderService;
    }

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
        // 复核通过：自动生成待审批订单（P1 B-003）。创建失败仅记录日志，不影响复核成功。
        try {
            createPendingOrder(apply);
        } catch (Exception e) {
            log.error("复核通过后自动生成待审批订单失败，申请单号：" + apply.getApplyNo(), e);
        }
        apply.setStatus("REVIEWED");
        apply.setReviewer(SecurityUtils.currentAccount());
        apply.setReviewTime(LocalDateTime.now());
        updateById(apply);
    }

    /**
     * 依据申请单自动生成待审批订单（幂等）：
     * <p>orderNo 优先沿用申请单号同日期序号（PA-xxx → PO-xxx）；申请单号非 PA- 前缀时按 PO-日期-序号 递增生成。
     * 品名/供应商/数量取自关联询价单；创建人为申请人；同 orderNo 已存在则跳过，避免重复创建。
     */
    private void createPendingOrder(Apply apply) {
        String orderNo = deriveOrderNo(apply);
        if (orderService.lambdaQuery().eq(PurchaseOrder::getOrderNo, orderNo).count() > 0) {
            return;
        }
        Inquiry inquiry = apply.getInquiryId() == null ? null : inquiryService.getById(apply.getInquiryId());
        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNo(orderNo);
        order.setSource("客服部");
        order.setStatus("PENDING_APPROVE");
        order.setCreator(apply.getApplicant());
        order.setCreateTime(LocalDateTime.now());
        if (inquiry != null) {
            order.setSupplierId(inquiry.getSupplierId());
            order.setSupplierName(inquiry.getSupplierName());
            order.setProductName(inquiry.getProductName());
            order.setQty(inquiry.getProductQty());
        }
        orderService.save(order);
    }

    /** 订单号生成：申请单号 PA- 前缀映射为 PO- 前缀；否则按 PO-日期-序号 递增（当天已有数量 + 1）。 */
    private String deriveOrderNo(Apply apply) {
        String applyNo = apply.getApplyNo();
        if (applyNo != null && applyNo.startsWith("PA-") && applyNo.length() > "PA-".length()) {
            return "PO-" + applyNo.substring("PA-".length());
        }
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = orderService.lambdaQuery().likeRight(PurchaseOrder::getOrderNo, "PO-" + today).count();
        return "PO-" + today + "-" + String.format("%03d", count + 1);
    }

    private Apply require(Long id) {
        Apply apply = getById(id);
        if (apply == null) {
            throw new IllegalStateException("采购申请单不存在：id=" + id);
        }
        return apply;
    }
}
