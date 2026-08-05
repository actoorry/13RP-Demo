package com.boyu.demo.module.base.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.base.entity.MobileConfig;
import com.boyu.demo.module.base.mapper.MobileConfigMapper;
import org.springframework.stereotype.Service;

/**
 * 移动端主营品种配置服务。
 */
@Service
public class MobileConfigService extends ServiceImpl<MobileConfigMapper, MobileConfig> {
}
