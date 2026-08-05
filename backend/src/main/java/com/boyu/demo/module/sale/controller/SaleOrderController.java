package com.boyu.demo.module.sale.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.sale.entity.SaleOrder;
import com.boyu.demo.module.sale.service.SaleOrderService;
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
 * 销售明细/订单接口：GET/POST/PUT/DELETE /api/sale/order。
 * <p>列表支持 order_no / customer_id / product_name 筛选。
 */
@RestController
@RequestMapping("/api/sale/order")
public class SaleOrderController {

    private final SaleOrderService service;

    public SaleOrderController(SaleOrderService service) {
        this.service = service;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String orderNo,
                                            @RequestParam(required = false) Long customerId,
                                            @RequestParam(required = false) String productName,
                                            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<SaleOrder> w = new LambdaQueryWrapper<>();
        w.like(orderNo != null && !orderNo.isBlank(), SaleOrder::getOrderNo, orderNo)
                .eq(customerId != null, SaleOrder::getCustomerId, customerId)
                .like(productName != null && !productName.isBlank(), SaleOrder::getProductName, productName)
                .and(keyword != null && !keyword.isBlank(),
                        wq -> wq.like(SaleOrder::getOrderNo, keyword)
                                .or().like(SaleOrder::getProductName, keyword))
                .orderByDesc(SaleOrder::getId);
        return Result.ok(PageQuery.toPageMap(service.page(new Page<>(query.getPage(), query.getSize()), w)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sale:order:add')")
    public Result<Void> create(@RequestBody SaleOrder entity) {
        service.save(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sale:order:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody SaleOrder entity) {
        entity.setId(id);
        service.updateById(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sale:order:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }
}
