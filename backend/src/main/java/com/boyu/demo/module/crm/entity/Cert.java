package com.boyu.demo.module.crm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 证照风控（crm_cert）：资料已核实 + 是否允许交易；未核实禁做业务单据。
 */
@Data
@TableName("crm_cert")
public class Cert {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 客户 id。 */
    private Long customerId;

    /** 证照类型。 */
    private String certType;

    /** 到期日期。 */
    private LocalDate expireDate;

    /** 注册资本。 */
    private BigDecimal registeredCapital;

    /** 税号。 */
    private String taxNo;

    /** 资料已核实：0 未核实 / 1 已核实。 */
    private Integer verifiedFlag;

    /** 是否允许交易：0 不允许 / 1 允许。 */
    private Integer tradeAllowedFlag;
}
