package com.boyu.demo.module.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyu.demo.module.base.entity.PackageStandard;
import org.apache.ibatis.annotations.Mapper;

/**
 * 合同包装验收标准 Mapper。
 */
@Mapper
public interface PackageStandardMapper extends BaseMapper<PackageStandard> {
}
