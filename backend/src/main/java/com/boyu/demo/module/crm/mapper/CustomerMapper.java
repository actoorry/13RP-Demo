package com.boyu.demo.module.crm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyu.demo.module.crm.entity.Customer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户基本资料 Mapper。
 */
@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {
}
