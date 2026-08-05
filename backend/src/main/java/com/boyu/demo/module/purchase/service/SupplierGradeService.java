package com.boyu.demo.module.purchase.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.purchase.entity.SupplierGrade;
import com.boyu.demo.module.purchase.mapper.SupplierGradeMapper;
import org.springframework.stereotype.Service;

/**
 * 供应商分级服务（战略/优选/考察/一般）。
 */
@Service
public class SupplierGradeService extends ServiceImpl<SupplierGradeMapper, SupplierGrade> {
}
