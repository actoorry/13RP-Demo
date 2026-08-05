package com.boyu.demo.module.crm.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.crm.entity.Lead;
import com.boyu.demo.module.crm.mapper.LeadMapper;
import org.springframework.stereotype.Service;

/**
 * 销售线索服务。
 */
@Service
public class LeadService extends ServiceImpl<LeadMapper, Lead> {
}
