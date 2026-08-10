package com.boyu.demo.module.org.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyu.demo.module.org.entity.SysPerson;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 人员（sys_person）Mapper：登录认证 + 员工管理。
 * <p>自定义 SQL 定义在 {@code resources/mapper/PersonMapper.xml}（XML 映射方式）。
 */
@Mapper
public interface PersonMapper extends BaseMapper<SysPerson> {

    /** 按登录账号查人员（未逻辑删除）。 */
    SysPerson selectByAccount(@Param("account") String account);
}
