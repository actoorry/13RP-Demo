package com.boyu.demo.module.finance.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.finance.entity.LabFee;
import com.boyu.demo.module.finance.service.LabFeeService;
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
 * 化验费接口：GET/POST/PUT/DELETE /api/finance/lab-fee。
 * <p>报告状态机：未上传 PENDING → 合格 PASS / 不合格 FAIL（报告上传后不可修改）；
 * 付款状态机：未付款 UNPAID → 已付款 PAID → 已冲账 REIMBURSED。
 * PUT 流转触发：body 仅含状态字段（{@code {reportStatus:'PASS'} / {payStatus:'PAID'}} 等，兼容 camelCase / snake_case）
 * 或 {@code action=report-pass/report-fail/pay/reimburse}；编辑表单提交完整实体走普通更新，状态以库中为准、置 null 防越权。
 * 非法迁移（未 PASS 付款、未付款冲账）由 Service 抛 IllegalStateException，Controller 捕获转 Result.error。
 */
@RestController
@RequestMapping("/api/finance/lab-fee")
public class LabFeeController {

    /** 状态流转允许出现的字段（其余字段出现视为普通编辑）。 */
    private static final Set<String> TRANSITION_KEYS = Set.of(
            "id", "reportStatus", "report_status", "payStatus", "pay_status", "action", "voucherNo", "voucher_no");

    private final LabFeeService service;
    private final ObjectMapper objectMapper;

    public LabFeeController(LabFeeService service, ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String reportStatus,
                                            @RequestParam(required = false) String payStatus,
                                            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<LabFee> w = new LambdaQueryWrapper<>();
        w.eq(reportStatus != null && !reportStatus.isBlank(), LabFee::getReportStatus, reportStatus)
                .eq(payStatus != null && !payStatus.isBlank(), LabFee::getPayStatus, payStatus)
                .and(keyword != null && !keyword.isBlank(),
                        wq -> wq.like(LabFee::getSampleNo, keyword)
                                .or().like(LabFee::getLabName, keyword))
                .orderByDesc(LabFee::getId);
        return Result.ok(PageQuery.toPageMap(service.page(new Page<>(query.getPage(), query.getSize()), w)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('finance:lab-fee:add')")
    public Result<Void> create(@RequestBody LabFee entity) {
        if (entity.getReportStatus() == null || entity.getReportStatus().isBlank()) {
            entity.setReportStatus("PENDING");
        }
        if (entity.getPayStatus() == null || entity.getPayStatus().isBlank()) {
            entity.setPayStatus("UNPAID");
        }
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:lab-fee:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        // 路径 id 优先；body 未带 id 时用路径 id（兼容两种提交方式）
        Long realId = id != null ? id : longVal(body.get("id"));
        if (realId == null) {
            return Result.error("缺少化验费单 id");
        }
        String reportStatus = str(body, "reportStatus");
        if (reportStatus == null) {
            reportStatus = str(body, "report_status");
        }
        String payStatus = str(body, "payStatus");
        if (payStatus == null) {
            payStatus = str(body, "pay_status");
        }
        String action = str(body, "action");
        String voucherNo = str(body, "voucherNo");
        if (voucherNo == null) {
            voucherNo = str(body, "voucher_no");
        }
        boolean flowIntent = action != null && !action.isBlank()
                || (reportStatus != null && !reportStatus.isBlank() && onlyTransitionFields(body))
                || (payStatus != null && !payStatus.isBlank() && onlyTransitionFields(body));
        if (flowIntent) {
            try {
                if ("report-pass".equalsIgnoreCase(action) || "PASS".equalsIgnoreCase(reportStatus)) {
                    service.reportPass(realId);
                } else if ("report-fail".equalsIgnoreCase(action) || "FAIL".equalsIgnoreCase(reportStatus)) {
                    service.reportFail(realId);
                } else if ("pay".equalsIgnoreCase(action) || "PAID".equalsIgnoreCase(payStatus)) {
                    service.pay(realId, voucherNo);
                } else if ("reimburse".equalsIgnoreCase(action) || "REIMBURSED".equalsIgnoreCase(payStatus)) {
                    service.reimburse(realId, voucherNo);
                } else {
                    return Result.error("不支持的化验费状态流转");
                }
                return Result.ok();
            } catch (IllegalStateException e) {
                return Result.error(e.getMessage());
            }
        }
        // 普通编辑（化验机构/凭证号等）：报告/付款状态只能走上方动作，置 null 防止越权流转
        LabFee entity = objectMapper.convertValue(body, LabFee.class);
        entity.setId(realId);
        entity.setReportStatus(null);
        entity.setPayStatus(null);
        service.updateById(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:lab-fee:delete')")
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
