package com.boyu.demo.module.crm.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.crm.entity.Customer;
import com.boyu.demo.module.crm.mapper.CustomerMapper;
import org.springframework.stereotype.Service;

/**
 * 客户基本资料服务。
 */
@Service
public class CustomerService extends ServiceImpl<CustomerMapper, Customer> {
}
