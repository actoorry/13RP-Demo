package com.boyu.demo.module.sale.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.sale.entity.SaleDailyReport;
import com.boyu.demo.module.sale.service.SaleDailyReportService;
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

import java.time.LocalDate;
import java.util.Map;

/**
 * 业务日报漏斗接口：GET/POST/PUT/DELETE /api/sale/daily-report。
 * <p>列表支持 report_date 筛选。
 */
@RestController
@RequestMapping("/api/sale/daily-report")
public class SaleDailyReportController {

    private final SaleDailyReportService service;

    public SaleDailyReportController(SaleDailyReportService service) {
        this.service = service;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) LocalDate reportDate,
                                            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<SaleDailyReport> w = new LambdaQueryWrapper<>();
        // keyword 保留但不参与过滤：业务日报实体全部为日期/数字/id 列（reportDate/contactCnt/leadCnt/dealCnt/orgId），
        // 无任何文本列可做 LIKE。keyword 参数继续接收（前端 SearchBar 统一传参）但不参与查询。
        // 前端该页搜索框应移除（无文本可搜），此项标记"前端后续处理"。
        w.eq(reportDate != null, SaleDailyReport::getReportDate, reportDate)
                .orderByDesc(SaleDailyReport::getReportDate);
        return Result.ok(PageQuery.toPageMap(service.page(new Page<>(query.getPage(), query.getSize()), w)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sale:daily-report:add')")
    public Result<Void> create(@RequestBody SaleDailyReport entity) {
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sale:daily-report:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody SaleDailyReport entity) {
        entity.setId(id);
        service.updateById(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sale:daily-report:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }
}
