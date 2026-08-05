package com.boyu.demo.module.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyu.demo.module.base.entity.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * 产品主数据 Mapper。
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
