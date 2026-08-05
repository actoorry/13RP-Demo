package com.boyu.demo.module.org.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.org.entity.OrgDict;
import com.boyu.demo.module.org.mapper.OrgDictMapper;
import org.springframework.stereotype.Service;

/**
 * 组织/岗位字典服务。
 */
@Service
public class OrgDictService extends ServiceImpl<OrgDictMapper, OrgDict> {
}
