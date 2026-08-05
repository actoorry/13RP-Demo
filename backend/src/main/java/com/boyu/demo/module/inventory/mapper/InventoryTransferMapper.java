package com.boyu.demo.module.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyu.demo.module.inventory.entity.InventoryTransfer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 调拨 Mapper。
 */
@Mapper
public interface InventoryTransferMapper extends BaseMapper<InventoryTransfer> {
}
