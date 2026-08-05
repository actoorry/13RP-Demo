package com.boyu.demo.module.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyu.demo.module.inventory.entity.InventoryBatch;
import org.apache.ibatis.annotations.Mapper;

/**
 * 批号管理 Mapper。
 */
@Mapper
public interface InventoryBatchMapper extends BaseMapper<InventoryBatch> {
}
