package com.boyu.demo.module.org.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyu.demo.module.org.entity.OrgDict;
import org.apache.ibatis.annotations.Mapper;

/**
 * 组织/岗位字典 Mapper。
 */
@Mapper
public interface OrgDictMapper extends BaseMapper<OrgDict> {
}
