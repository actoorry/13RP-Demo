package com.boyu.demo.module.crm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.crm.entity.Cert;
import com.boyu.demo.module.crm.service.CertService;
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

/**
 * 证照风控接口：GET/POST/PUT/DELETE /api/crm/cert（资料已核实 + 是否允许交易）。
 * <p>风控前置校验：资料未核实的证照禁止允许交易（Service 抛 IllegalStateException，此处捕获转 Result.error）。
 */
@RestController
@RequestMapping("/api/crm/cert")
public class CertController {

    private final CertService service;

    public CertController(CertService service) {
        this.service = service;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) Integer verifiedFlag,
                                            @RequestParam(required = false) Integer tradeAllowedFlag,
                                            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Cert> w = new LambdaQueryWrapper<>();
        w.eq(verifiedFlag != null, Cert::getVerifiedFlag, verifiedFlag)
                .eq(tradeAllowedFlag != null, Cert::getTradeAllowedFlag, tradeAllowedFlag)
                .and(keyword != null && !keyword.isBlank(),
                        wq -> wq.like(Cert::getCertType, keyword)
                                .or().like(Cert::getTaxNo, keyword))
                .orderByDesc(Cert::getId);
        return Result.ok(PageQuery.toPageMap(service.page(new Page<>(query.getPage(), query.getSize()), w)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('crm:cert:add')")
    public Result<Void> create(@RequestBody Cert entity) {
        try {
            service.save(entity);
            return Result.ok();
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('crm:cert:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Cert entity) {
        entity.setId(id);
        try {
            service.updateById(entity);
            return Result.ok();
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('crm:cert:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }
}
