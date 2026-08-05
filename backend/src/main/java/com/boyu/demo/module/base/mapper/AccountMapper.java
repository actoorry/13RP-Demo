package com.boyu.demo.module.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyu.demo.module.base.entity.Account;
import org.apache.ibatis.annotations.Mapper;

/**
 * 账套 Mapper。
 */
@Mapper
public interface AccountMapper extends BaseMapper<Account> {
}
