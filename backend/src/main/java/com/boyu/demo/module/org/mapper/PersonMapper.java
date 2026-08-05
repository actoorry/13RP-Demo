package com.boyu.demo.module.org.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyu.demo.module.org.entity.SysPerson;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 人员（sys_person）Mapper：登录认证 + 员工管理。
 */
@Mapper
public interface PersonMapper extends BaseMapper<SysPerson> {

    /** 按登录账号查人员（未逻辑删除）。 */
    @Select("SELECT * FROM sys_person WHERE account = #{account} AND deleted = 0")
    SysPerson selectByAccount(@Param("account") String account);
}
