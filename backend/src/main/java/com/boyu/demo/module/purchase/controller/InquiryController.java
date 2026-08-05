package com.boyu.demo.module.purchase.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.purchase.entity.Inquiry;
import com.boyu.demo.module.purchase.service.InquiryService;
import org.springframework.security.access.prepost.PreAuthorize;
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
 * 询价管理接口：GET/POST/PUT /api/purchase/inquiry（急询价/指定询价）。
 * <p>PUT 支持状态流转：status=RECEIVED 接收；status=REPLIED 反馈；否则普通更新。
 */
@RestController
@RequestMapping("/api/purchase/inquiry")
public class InquiryController {

    private final InquiryService service;

    public InquiryController(InquiryService service) {
        this.service = service;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String inquiryType,
                                            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<Inquiry> w = new LambdaQueryWrapper<>();
        w.eq(inquiryType != null && !inquiryType.isBlank(), Inquiry::getInquiryType, inquiryType)
                .eq(status != null && !status.isBlank(), Inquiry::getStatus, status)
                .orderByDesc(Inquiry::getId);
        return Result.ok(PageQuery.toPageMap(service.page(new Page<>(query.getPage(), query.getSize()), w)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('purchase:inquiry:add')")
    public Result<Void> create(@RequestBody Inquiry entity) {
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus("CREATED");
        }
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('purchase:inquiry:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        // 路径 id 优先；body 未带 id 时用路径 id（兼容两种提交方式）
        Long realId = id != null ? id : longVal(body.get("id"));
        if (realId == null) {
            return Result.error("缺少询价单 id");
        }
        String status = str(body, "status");
        if ("RECEIVED".equalsIgnoreCase(status)) {
            service.receive(realId);
        } else if ("REPLIED".equalsIgnoreCase(status)) {
            service.reply(realId);
        } else {
            return Result.error("不支持的询价状态流转：" + status);
        }
        return Result.ok();
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
