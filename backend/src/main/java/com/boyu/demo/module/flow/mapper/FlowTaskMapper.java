package com.boyu.demo.module.flow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyu.demo.module.flow.entity.FlowTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 流程待办/已办 Mapper。
 */
@Mapper
public interface FlowTaskMapper extends BaseMapper<FlowTask> {
}
