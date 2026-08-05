package com.boyu.demo.module.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyu.demo.module.finance.entity.Invoice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 发票管理 Mapper。
 */
@Mapper
public interface InvoiceMapper extends BaseMapper<Invoice> {
}
