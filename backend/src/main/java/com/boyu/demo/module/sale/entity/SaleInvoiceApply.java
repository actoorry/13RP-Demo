package com.boyu.demo.module.sale.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 开票申请（sale_invoice_apply）：申请 APPLIED → 待开 PENDING → 已开 ISSUED。
 */
@Data
@TableName("sale_invoice_apply")
public class SaleInvoiceApply {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String applyNo;

    private Long customerId;

    private String invoiceNo;

    /** 申请 APPLIED / 待开 PENDING / 已开 ISSUED。 */
    private String status;

    private String creator;
}
