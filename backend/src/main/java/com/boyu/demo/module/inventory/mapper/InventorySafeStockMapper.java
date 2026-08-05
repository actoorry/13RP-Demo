package com.boyu.demo.module.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyu.demo.module.inventory.entity.InventorySafeStock;
import org.apache.ibatis.annotations.Mapper;

/**
 * 安全库存 Mapper。
 */
@Mapper
public interface InventorySafeStockMapper extends BaseMapper<InventorySafeStock> {
}
