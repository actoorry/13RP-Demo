package com.boyu.demo.module.purchase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyu.demo.module.purchase.entity.Inquiry;
import org.apache.ibatis.annotations.Mapper;

/**
 * 询价管理 Mapper。
 */
@Mapper
public interface InquiryMapper extends BaseMapper<Inquiry> {
}
