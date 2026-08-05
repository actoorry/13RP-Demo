package com.boyu.demo.module.org.controller;

import com.boyu.demo.module.org.entity.SysPerson;

import java.util.List;

/**
 * 员工新增/更新请求体（扁平字段 + 角色 id 列表）。
 */
public record EmployeeRequest(
        Long id,
        Long institutionId,
        String account,
        String password,
        String name,
        String phone,
        String dept,
        String position,
        Integer status,
        List<Long> roleIds
) {
    /** 转换为 sys_person 实体。 */
    public SysPerson toPerson() {
        SysPerson p = new SysPerson();
        p.setId(id);
        p.setInstitutionId(institutionId);
        p.setAccount(account);
        p.setPassword(password);
        p.setName(name);
        p.setPhone(phone);
        p.setDept(dept);
        p.setPosition(position);
        p.setStatus(status);
        return p;
    }
}
