package com.boyu.demo.module.org.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.common.PageQuery;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.org.entity.SysPerson;
import com.boyu.demo.module.org.service.EmployeeService;
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
 * 员工管理接口（契约 §3.4）：GET/POST/PUT /api/org/employee。
 */
@RestController
@RequestMapping("/api/org/employee")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query,
                                            @RequestParam(required = false) String keyword) {
        Page<SysPerson> page = service.page(new Page<>(query.getPage(), query.getSize()), keyword);
        return Result.ok(PageQuery.toPageMap(page));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('org:person:add')")
    public Result<Void> create(@RequestBody EmployeeRequest req) {
        service.create(req.toPerson(), req.roleIds());
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('org:person:edit')")
    public Result<Void> update(@PathVariable Long id, @RequestBody EmployeeRequest req) {
        SysPerson person = req.toPerson();
        if (person.getId() == null) {
            person.setId(id);
        }
        service.update(person, req.roleIds());
        return Result.ok();
    }
}
