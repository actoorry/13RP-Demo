package com.boyu.demo.module.base.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.base.entity.MaterialElement;
import com.boyu.demo.module.base.mapper.MaterialElementMapper;
import org.springframework.stereotype.Service;

/**
 * 材质元素服务（含量区间/牌号独立标记）。
 */
@Service
public class MaterialElementService extends ServiceImpl<MaterialElementMapper, MaterialElement> {
}
