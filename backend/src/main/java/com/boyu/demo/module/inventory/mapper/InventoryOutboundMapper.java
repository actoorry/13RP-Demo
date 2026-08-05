package com.boyu.demo.module.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyu.demo.module.inventory.entity.InventoryOutbound;
import org.apache.ibatis.annotations.Mapper;

/**
 * 出库管理 Mapper。
 */
@Mapper
public interface InventoryOutboundMapper extends BaseMapper<InventoryOutbound> {
}
