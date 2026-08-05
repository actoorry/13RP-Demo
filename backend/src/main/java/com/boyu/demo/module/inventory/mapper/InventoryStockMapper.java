package com.boyu.demo.module.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyu.demo.module.inventory.entity.InventoryStock;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存统计 Mapper。
 */
@Mapper
public interface InventoryStockMapper extends BaseMapper<InventoryStock> {
}
