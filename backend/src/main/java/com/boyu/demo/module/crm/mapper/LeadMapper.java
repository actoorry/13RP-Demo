package com.boyu.demo.module.crm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyu.demo.module.crm.entity.Lead;
import org.apache.ibatis.annotations.Mapper;

/**
 * 销售线索 Mapper。
 */
@Mapper
public interface LeadMapper extends BaseMapper<Lead> {
}
