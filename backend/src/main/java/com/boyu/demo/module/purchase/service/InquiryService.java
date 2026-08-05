package com.boyu.demo.module.purchase.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.purchase.entity.Inquiry;
import com.boyu.demo.module.purchase.mapper.InquiryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 询价管理服务：急询价/指定询价；状态机 发起 CREATED → 接收 RECEIVED → 反馈 REPLIED。
 */
@Service
public class InquiryService extends ServiceImpl<InquiryMapper, Inquiry> {

    /** 接收（集采部接收指定询价单）。 */
    @Transactional
    public void receive(Long id) {
        Inquiry inquiry = getById(id);
        if (inquiry != null && "CREATED".equals(inquiry.getStatus())) {
            inquiry.setStatus("RECEIVED");
            updateById(inquiry);
        }
    }

    /** 反馈（仅已接收 RECEIVED 的询价可反馈报价）。 */
    @Transactional
    public void reply(Long id) {
        Inquiry inquiry = getById(id);
        if (inquiry == null || !"RECEIVED".equals(inquiry.getStatus())) {
            return;
        }
        inquiry.setStatus("REPLIED");
        inquiry.setReplyTime(LocalDateTime.now());
        updateById(inquiry);
    }
}
