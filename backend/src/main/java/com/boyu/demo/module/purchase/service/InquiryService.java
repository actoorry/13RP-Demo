package com.boyu.demo.module.purchase.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.purchase.entity.Inquiry;
import com.boyu.demo.module.purchase.mapper.InquiryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 询价管理服务：急询价/指定询价；状态机 发起 CREATED → 接收 RECEIVED → 反馈 REPLIED。
 * 记录不存在 / 状态非法时抛 IllegalStateException（Controller 捕获转 Result.error）。
 */
@Service
public class InquiryService extends ServiceImpl<InquiryMapper, Inquiry> {

    /** 接收（集采部接收指定询价单，仅发起 CREATED 的询价可接收）。 */
    @Transactional
    public void receive(Long id) {
        Inquiry inquiry = require(id);
        if (!"CREATED".equals(inquiry.getStatus())) {
            throw new IllegalStateException("仅发起(CREATED)状态的询价单可接收，当前：" + inquiry.getStatus());
        }
        inquiry.setStatus("RECEIVED");
        updateById(inquiry);
    }

    /** 反馈（仅已接收 RECEIVED 的询价可反馈报价）。 */
    @Transactional
    public void reply(Long id) {
        Inquiry inquiry = require(id);
        if (!"RECEIVED".equals(inquiry.getStatus())) {
            throw new IllegalStateException("仅已接收(RECEIVED)状态的询价单可反馈，当前：" + inquiry.getStatus());
        }
        inquiry.setStatus("REPLIED");
        inquiry.setReplyTime(LocalDateTime.now());
        updateById(inquiry);
    }

    private Inquiry require(Long id) {
        Inquiry inquiry = getById(id);
        if (inquiry == null) {
            throw new IllegalStateException("询价单不存在：id=" + id);
        }
        return inquiry;
    }
}
