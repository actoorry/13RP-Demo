package com.boyu.demo.module.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyu.demo.module.inventory.entity.InventoryCheck;
import org.apache.ibatis.annotations.Mapper;

/**
 * 盘点 Mapper。
 */
@Mapper
public interface InventoryCheckMapper extends BaseMapper<InventoryCheck> {
}
