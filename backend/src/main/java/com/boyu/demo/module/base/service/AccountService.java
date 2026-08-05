package com.boyu.demo.module.base.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.base.entity.Account;
import com.boyu.demo.module.base.mapper.AccountMapper;
import org.springframework.stereotype.Service;

/**
 * 账套管理服务（多账套切换，数据隔离边界）。
 */
@Service
public class AccountService extends ServiceImpl<AccountMapper, Account> {
}
