package com.boyu.demo.module.base.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.base.entity.PackageStandard;
import com.boyu.demo.module.base.mapper.PackageStandardMapper;
import org.springframework.stereotype.Service;

/**
 * 合同包装验收标准服务。
 */
@Service
public class PackageStandardService extends ServiceImpl<PackageStandardMapper, PackageStandard> {
}
