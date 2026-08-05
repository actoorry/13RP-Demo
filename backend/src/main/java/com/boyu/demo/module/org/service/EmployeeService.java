package com.boyu.demo.module.org.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyu.demo.module.org.entity.SysPerson;
import com.boyu.demo.module.org.mapper.AuthMapper;
import com.boyu.demo.module.org.mapper.PersonMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 员工管理服务（sys_person）：新增/更新时同步人员-角色关联。
 */
@Service
public class EmployeeService {

    private final PersonMapper personMapper;
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(PersonMapper personMapper, AuthMapper authMapper, PasswordEncoder passwordEncoder) {
        this.personMapper = personMapper;
        this.authMapper = authMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /** 员工分页查询（按姓名/账号/手机号关键字过滤）。 */
    public Page<SysPerson> page(Page<SysPerson> page, String keyword) {
        LambdaQueryWrapper<SysPerson> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            w.and(q -> q.like(SysPerson::getName, keyword)
                    .or().like(SysPerson::getAccount, keyword)
                    .or().like(SysPerson::getPhone, keyword));
        }
        w.orderByAsc(SysPerson::getId);
        return personMapper.selectPage(page, w);
    }

    /** 新增员工：密码默认 123456（BCrypt），绑定角色。 */
    @Transactional
    public void create(SysPerson person, List<Long> roleIds) {
        if (person.getPassword() == null || person.getPassword().isBlank()) {
            person.setPassword("123456");
        }
        person.setPassword(passwordEncoder.encode(person.getPassword()));
        if (person.getStatus() == null) {
            person.setStatus(1);
        }
        personMapper.insert(person);
        bindRoles(person.getId(), roleIds);
    }

    /** 更新员工：密码留空则不修改；roleIds 非空则重建角色。 */
    @Transactional
    public void update(SysPerson person, List<Long> roleIds) {
        if (person.getPassword() != null && !person.getPassword().isBlank()) {
            person.setPassword(passwordEncoder.encode(person.getPassword()));
        } else {
            person.setPassword(null);
        }
        personMapper.updateById(person);
        if (roleIds != null) {
            authMapper.deletePersonRoles(person.getId());
            bindRoles(person.getId(), roleIds);
        }
    }

    private void bindRoles(Long personId, List<Long> roleIds) {
        if (roleIds == null) {
            return;
        }
        for (Long rid : roleIds) {
            authMapper.insertPersonRole(personId, rid);
        }
    }
}
