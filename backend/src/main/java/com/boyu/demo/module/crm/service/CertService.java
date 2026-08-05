package com.boyu.demo.module.crm.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.crm.entity.Cert;
import com.boyu.demo.module.crm.mapper.CertMapper;
import org.springframework.stereotype.Service;

/**
 * 证照风控服务。
 * <p>风控前置校验：资料未核实（verifiedFlag=0）的证照禁止允许交易（tradeAllowedFlag=1），
 * 非法开启由 Service 抛 IllegalStateException（Controller 捕获转 Result.error）。
 */
@Service
public class CertService extends ServiceImpl<CertMapper, Cert> {

    @Override
    public boolean save(Cert entity) {
        if (Integer.valueOf(1).equals(entity.getTradeAllowedFlag())
                && !Integer.valueOf(1).equals(entity.getVerifiedFlag())) {
            throw new IllegalStateException("资料未核实，禁止允许交易");
        }
        return super.save(entity);
    }

    @Override
    public boolean updateById(Cert entity) {
        if (Integer.valueOf(1).equals(entity.getTradeAllowedFlag())) {
            Cert current = getById(entity.getId());
            if (current == null || !Integer.valueOf(1).equals(current.getVerifiedFlag())) {
                throw new IllegalStateException("资料未核实，禁止允许交易");
            }
        }
        return super.updateById(entity);
    }
}
