package com.boyu.demo.module.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyu.demo.module.inventory.entity.InventoryInbound;
import org.apache.ibatis.annotations.Mapper;

/**
 * 入库管理 Mapper。
 */
@Mapper
public interface InventoryInboundMapper extends BaseMapper<InventoryInbound> {
}
