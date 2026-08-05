package com.boyu.demo.module.finance.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.finance.entity.ArAp;
import com.boyu.demo.module.finance.mapper.ArApMapper;
import org.springframework.stereotype.Service;

/**
 * 应收应付服务。
 */
@Service
public class ArApService extends ServiceImpl<ArApMapper, ArAp> {
}
