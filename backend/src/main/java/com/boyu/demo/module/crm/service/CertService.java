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
        Cert current = getById(entity.getId());
        if (Integer.valueOf(1).equals(entity.getTradeAllowedFlag())) {
            if (current == null || !Integer.valueOf(1).equals(current.getVerifiedFlag())) {
                throw new IllegalStateException("资料未核实，禁止允许交易");
            }
        }
        // 越权防护：已允许交易的证照，若显式取消"资料已核实"，必须先取消"允许交易"，
        // 避免出现 verifiedFlag=0 与 tradeAllowedFlag=1 不一致状态
        if (current != null
                && Integer.valueOf(1).equals(current.getTradeAllowedFlag())
                && entity.getVerifiedFlag() != null
                && Integer.valueOf(0).equals(entity.getVerifiedFlag())) {
            throw new IllegalStateException("资料已核实标记被取消，请先取消允许交易");
        }
        return super.updateById(entity);
    }
}
