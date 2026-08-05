-- =====================================================================
-- 13RP-Demo 数据库初始化脚本（V0.4 九大业务域核心表）
-- 库：boyu_demo（MySQL 8）
-- 约定：所有表 IF NOT EXISTS；幂等，可重复执行
-- 决策演示相关（orchestrator/WebSocket/PrecomputedData）不依赖数据库，
-- 本脚本仅为管理端九大业务域建表，保留 V0.3 五模块基础表。
-- =====================================================================

-- ===================== V0.3 保留表：组织与权限基础 =====================

CREATE TABLE IF NOT EXISTS sys_institution (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '机构 id',
    parent_id   BIGINT       DEFAULT 0 COMMENT '父机构 id',
    name        VARCHAR(100) NOT NULL COMMENT '机构名称',
    sort        INT          DEFAULT 0 COMMENT '排序',
    deleted     TINYINT      DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '机构（树）';

CREATE TABLE IF NOT EXISTS sys_person (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '人员 id',
    institution_id BIGINT       DEFAULT 0 COMMENT '归属机构 id',
    avatar         VARCHAR(255) DEFAULT NULL COMMENT '头像',
    account        VARCHAR(50)  NOT NULL COMMENT '登录账号',
    password       VARCHAR(100) NOT NULL DEFAULT '' COMMENT 'BCrypt 密码',
    name           VARCHAR(50)  NOT NULL COMMENT '姓名',
    phone          VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    dept           VARCHAR(50)  DEFAULT NULL COMMENT '部门',
    position       VARCHAR(50)  DEFAULT NULL COMMENT '岗位',
    status         TINYINT      DEFAULT 1 COMMENT '状态 1 启用 0 停用',
    deleted        TINYINT      DEFAULT 0 COMMENT '逻辑删除',
    create_time    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_account (account)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '人员（登录用户/员工）';

CREATE TABLE IF NOT EXISTS sys_role (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色 id',
    name    VARCHAR(50) NOT NULL COMMENT '角色名称',
    sort    INT         DEFAULT 0 COMMENT '排序',
    deleted TINYINT     DEFAULT 0 COMMENT '逻辑删除'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '角色';

CREATE TABLE IF NOT EXISTS sys_menu (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '菜单 id',
    parent_id BIGINT       DEFAULT 0 COMMENT '父菜单 id',
    name      VARCHAR(50)  NOT NULL COMMENT '菜单名称',
    type      VARCHAR(10)  NOT NULL DEFAULT 'MENU' COMMENT 'MENU 菜单 / BUTTON 按钮',
    path      VARCHAR(100) DEFAULT NULL COMMENT '路由/资源路径',
    perms     VARCHAR(100) DEFAULT NULL COMMENT '权限码（如 base:product:edit）',
    sort      INT          DEFAULT 0 COMMENT '排序'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '菜单/按钮权限';

CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id BIGINT NOT NULL COMMENT '角色 id',
    menu_id BIGINT NOT NULL COMMENT '菜单 id',
    PRIMARY KEY (role_id, menu_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '角色-菜单关联';

CREATE TABLE IF NOT EXISTS sys_person_role (
    person_id BIGINT NOT NULL COMMENT '人员 id',
    role_id   BIGINT NOT NULL COMMENT '角色 id',
    PRIMARY KEY (person_id, role_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '人员-角色关联';

-- ===================== V0.3 保留表：商品/品类/属性/单位/合同要素 =====================

CREATE TABLE IF NOT EXISTS commodity_channel (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '频道 id',
    name        VARCHAR(100) NOT NULL COMMENT '频道名称',
    status      VARCHAR(20)  DEFAULT NULL COMMENT '状态',
    creator     VARCHAR(50)  DEFAULT NULL COMMENT '创建人',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '商品频道';

CREATE TABLE IF NOT EXISTS commodity_category (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '品类 id',
    parent_id  BIGINT       DEFAULT 0 COMMENT '父品类 id',
    channel_id BIGINT       DEFAULT 0 COMMENT '频道 id',
    name       VARCHAR(100) NOT NULL COMMENT '品类名称',
    sort       INT          DEFAULT 0 COMMENT '排序',
    is_show    TINYINT      DEFAULT 1 COMMENT '是否展示'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '商品品类';

CREATE TABLE IF NOT EXISTS commodity_attribute (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '属性 id',
    category_id  BIGINT       DEFAULT 0 COMMENT '品类 id',
    name         VARCHAR(50)  NOT NULL COMMENT '属性名称',
    param_values TEXT         DEFAULT NULL COMMENT '参数值',
    attr_values  TEXT         DEFAULT NULL COMMENT '属性值',
    enabled      TINYINT      DEFAULT 1 COMMENT '启用',
    sort         INT          DEFAULT 0 COMMENT '排序'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '商品属性';

CREATE TABLE IF NOT EXISTS commodity_rule (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '规则 id',
    category_id  BIGINT      DEFAULT 0 COMMENT '品类 id',
    attr_name    VARCHAR(50) DEFAULT NULL COMMENT '属性名',
    attr_value   VARCHAR(100) DEFAULT NULL COMMENT '属性值',
    publish_time DATETIME    DEFAULT NULL COMMENT '发布时间'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '商品规则';

CREATE TABLE IF NOT EXISTS commodity_unit (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '单位 id',
    category_id BIGINT      DEFAULT 0 COMMENT '品类 id',
    name        VARCHAR(20) NOT NULL COMMENT '单位名称',
    enabled     TINYINT     DEFAULT 1 COMMENT '启用'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '计量单位';

CREATE TABLE IF NOT EXISTS commodity_unit_conversion (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '换算 id',
    unit_id        BIGINT         DEFAULT 0 COMMENT '单位 id',
    target_unit_id BIGINT         DEFAULT 0 COMMENT '目标单位 id',
    ratio          DECIMAL(12, 4) DEFAULT 1 COMMENT '换算比例',
    enabled        TINYINT        DEFAULT 1 COMMENT '启用'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '单位换算';

CREATE TABLE IF NOT EXISTS commodity_contract_element (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '合同要素 id',
    name             VARCHAR(100)  NOT NULL COMMENT '要素名称',
    channel_id       BIGINT        DEFAULT 0 COMMENT '频道 id',
    settle_method    VARCHAR(50)   DEFAULT NULL COMMENT '结算方式',
    freight_bearer   VARCHAR(50)   DEFAULT NULL COMMENT '运费承担方',
    supplier_note    VARCHAR(255)  DEFAULT NULL COMMENT '供应商备注',
    pay_method       VARCHAR(50)   DEFAULT NULL COMMENT '付款方式',
    available_qty    DECIMAL(12, 2) DEFAULT 0 COMMENT '可用数量',
    creator          VARCHAR(50)   DEFAULT NULL COMMENT '创建人',
    status           VARCHAR(20)   DEFAULT NULL COMMENT '状态'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '合同要素';

CREATE TABLE IF NOT EXISTS commodity_contract_element_item (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '要素项 id',
    element_id BIGINT      DEFAULT 0 COMMENT '合同要素 id',
    name       VARCHAR(50) DEFAULT NULL COMMENT '项名称',
    editable   TINYINT     DEFAULT 1 COMMENT '可编辑'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '合同要素项';

-- ===================== V0.3 保留表：CRM 基础 =====================

CREATE TABLE IF NOT EXISTS crm_lead (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '线索 id',
    name             VARCHAR(100) DEFAULT NULL COMMENT '名称',
    source           VARCHAR(50)  DEFAULT NULL COMMENT '来源',
    company_type     VARCHAR(50)  DEFAULT NULL COMMENT '公司类型',
    phone            VARCHAR(20)  DEFAULT NULL COMMENT '手机',
    tel              VARCHAR(20)  DEFAULT NULL COMMENT '电话',
    email            VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    address          VARCHAR(255) DEFAULT NULL COMMENT '地址',
    industry         VARCHAR(50)  DEFAULT NULL COMMENT '行业',
    level            VARCHAR(20)  DEFAULT NULL COMMENT '等级',
    next_contact_time DATETIME    DEFAULT NULL COMMENT '下次联系时间',
    remark           VARCHAR(500) DEFAULT NULL COMMENT '备注',
    owner_id         BIGINT       DEFAULT 0 COMMENT '负责人 id',
    follow_flag      TINYINT      DEFAULT 0 COMMENT '跟进标记',
    converted_flag   TINYINT      DEFAULT 0 COMMENT '已转化',
    last_follow_time DATETIME     DEFAULT NULL COMMENT '最后跟进时间',
    extra_fields     JSON         DEFAULT NULL COMMENT '扩展字段'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '销售线索';

CREATE TABLE IF NOT EXISTS crm_customer (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '客户 id',
    name             VARCHAR(100) DEFAULT NULL COMMENT '名称',
    source           VARCHAR(50)  DEFAULT NULL COMMENT '来源',
    company_type     VARCHAR(50)  DEFAULT NULL COMMENT '公司类型',
    phone            VARCHAR(20)  DEFAULT NULL COMMENT '手机',
    tel              VARCHAR(20)  DEFAULT NULL COMMENT '电话',
    email            VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    address          VARCHAR(255) DEFAULT NULL COMMENT '地址',
    industry         VARCHAR(50)  DEFAULT NULL COMMENT '行业',
    level            VARCHAR(20)  DEFAULT NULL COMMENT '等级',
    next_contact_time DATETIME    DEFAULT NULL COMMENT '下次联系时间',
    remark           VARCHAR(500) DEFAULT NULL COMMENT '备注',
    owner_id         BIGINT       DEFAULT 0 COMMENT '负责人 id',
    follow_flag      TINYINT      DEFAULT 0 COMMENT '跟进标记',
    converted_flag   TINYINT      DEFAULT 0 COMMENT '已转化',
    last_follow_time DATETIME     DEFAULT NULL COMMENT '最后跟进时间',
    extra_fields     JSON         DEFAULT NULL COMMENT '扩展字段'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '客户';

CREATE TABLE IF NOT EXISTS crm_pool_rule (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '规则 id',
    type        VARCHAR(20)  DEFAULT NULL COMMENT '类型',
    rule_name   VARCHAR(100) DEFAULT NULL COMMENT '规则名称',
    recycle_days INT         DEFAULT 0 COMMENT '回收天数',
    enabled     TINYINT      DEFAULT 1 COMMENT '启用'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '客户池规则';

CREATE TABLE IF NOT EXISTS crm_custom_field_def (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '字段定义 id',
    module      VARCHAR(20)  DEFAULT NULL COMMENT '所属模块',
    field_key   VARCHAR(50)  DEFAULT NULL COMMENT '字段 key',
    field_label VARCHAR(50)  DEFAULT NULL COMMENT '字段标签',
    field_type  VARCHAR(20)  DEFAULT NULL COMMENT '字段类型',
    options     VARCHAR(500) DEFAULT NULL COMMENT '选项'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '自定义字段定义';

-- ===================== V0.3 保留表：组织-员工/综合管理 =====================

CREATE TABLE IF NOT EXISTS op_user (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户 id',
    name              VARCHAR(50)  DEFAULT NULL COMMENT '姓名',
    phone             VARCHAR(20)  DEFAULT NULL COMMENT '手机',
    real_name_flag    TINYINT      DEFAULT 0 COMMENT '实名标记',
    company_name      VARCHAR(100) DEFAULT NULL COMMENT '公司名称',
    company_cert_flag TINYINT      DEFAULT 0 COMMENT '公司认证标记',
    status            VARCHAR(10)  DEFAULT NULL COMMENT '状态',
    register_time     DATETIME     DEFAULT NULL COMMENT '注册时间',
    user_type         VARCHAR(20)  DEFAULT NULL COMMENT '用户类型',
    channel           VARCHAR(20)  DEFAULT NULL COMMENT '渠道',
    erp_sync_status   VARCHAR(20)  DEFAULT NULL COMMENT 'ERP 同步状态',
    erp_company_name  VARCHAR(100) DEFAULT NULL COMMENT 'ERP 公司名',
    org               VARCHAR(100) DEFAULT NULL COMMENT '组织',
    owner             VARCHAR(50)  DEFAULT NULL COMMENT '负责人'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '运营用户';

CREATE TABLE IF NOT EXISTS op_cert (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '认证 id',
    user_id      BIGINT       DEFAULT 0 COMMENT '用户 id',
    company_name VARCHAR(100) DEFAULT NULL COMMENT '公司名称',
    status       VARCHAR(20)  DEFAULT NULL COMMENT '状态',
    cert_time    DATETIME     DEFAULT NULL COMMENT '认证时间'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '运营认证';

CREATE TABLE IF NOT EXISTS op_shared_data (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '共享数据 id',
    enabled TINYINT DEFAULT 1 COMMENT '启用'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '共享数据';

CREATE TABLE IF NOT EXISTS op_multi_head (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '多名头 id',
    group_name  VARCHAR(100) DEFAULT NULL COMMENT '组名称',
    company_name VARCHAR(100) DEFAULT NULL COMMENT '公司名称',
    creator     VARCHAR(50)  DEFAULT NULL COMMENT '创建人',
    create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
    progress    VARCHAR(20)  DEFAULT NULL COMMENT '进度'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '多名头';

-- ===================== V0.3 保留表：数据统计 =====================

CREATE TABLE IF NOT EXISTS stat_enterprise (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '企业 id',
    name        VARCHAR(100) DEFAULT NULL COMMENT '企业名称',
    owner       VARCHAR(50)  DEFAULT NULL COMMENT '负责人',
    category1   VARCHAR(50)  DEFAULT NULL COMMENT '分类 1',
    category2   VARCHAR(50)  DEFAULT NULL COMMENT '分类 2',
    category3   VARCHAR(50)  DEFAULT NULL COMMENT '分类 3',
    attr_name   VARCHAR(50)  DEFAULT NULL COMMENT '属性名',
    attr_value  VARCHAR(100) DEFAULT NULL COMMENT '属性值',
    contact     VARCHAR(50)  DEFAULT NULL COMMENT '联系人',
    phone       VARCHAR(20)  DEFAULT NULL COMMENT '电话',
    create_time DATETIME     DEFAULT NULL COMMENT '创建时间'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '企业库';

CREATE TABLE IF NOT EXISTS stat_snapshot (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '快照 id',
    snapshot_date  DATE           DEFAULT NULL COMMENT '快照日期',
    metric_key     VARCHAR(50)    DEFAULT NULL COMMENT '指标 key',
    metric_value   DECIMAL(12, 2) DEFAULT 0 COMMENT '指标值',
    yesterday_value DECIMAL(12, 2) DEFAULT 0 COMMENT '昨日值'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '指标快照';

-- ===================== V0.4 新增：基础数据域 =====================

CREATE TABLE IF NOT EXISTS base_account (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '账套 id',
    name        VARCHAR(50) NOT NULL COMMENT '账套名称（博宇股份/藏博/沈博/总部）',
    code        VARCHAR(20) DEFAULT NULL COMMENT '账套编码',
    status      TINYINT     DEFAULT 1 COMMENT '状态 1 启用 0 停用',
    remark      VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_time DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '账套（多账套切换，数据隔离边界）';

CREATE TABLE IF NOT EXISTS base_product (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '产品 id',
    account_id   BIGINT       DEFAULT 0 COMMENT '账套 id',
    name         VARCHAR(100) NOT NULL COMMENT '品名',
    grade        VARCHAR(50)  DEFAULT NULL COMMENT '牌号',
    material     VARCHAR(50)  DEFAULT NULL COMMENT '材质（材质元素关联）',
    spec         VARCHAR(100) DEFAULT NULL COMMENT '规格',
    brand_origin VARCHAR(100) DEFAULT NULL COMMENT '品牌/产地',
    other        VARCHAR(255) DEFAULT NULL COMMENT '其他',
    parent_id    BIGINT       DEFAULT 0 COMMENT '父级 id（品名→牌号→材质树）',
    sort         INT          DEFAULT 0 COMMENT '排序',
    status       TINYINT      DEFAULT 1 COMMENT '状态 1 正常 0 作废',
    deleted      TINYINT      DEFAULT 0 COMMENT '逻辑删除',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '产品主数据（品名→牌号→材质元素 六项维护）';

CREATE TABLE IF NOT EXISTS base_material_element (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '元素 id',
    symbol            VARCHAR(10)  NOT NULL COMMENT '元素符号',
    sort              INT          DEFAULT 0 COMMENT '排序',
    common_value      VARCHAR(50)  DEFAULT NULL COMMENT '常用值/含量',
    range_min         DECIMAL(10, 2) DEFAULT NULL COMMENT '含量区间下限',
    range_max         DECIMAL(10, 2) DEFAULT NULL COMMENT '含量区间上限',
    grade_independent TINYINT      DEFAULT 0 COMMENT '牌号材质元素独立标记',
    remark            VARCHAR(255) DEFAULT NULL COMMENT '备注'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '材质元素（含量区间/牌号独立标记）';

CREATE TABLE IF NOT EXISTS base_package_standard (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '标准 id',
    package_name        VARCHAR(50) NOT NULL COMMENT '包装名称',
    damage_compensation VARCHAR(255) DEFAULT NULL COMMENT '破损赔偿',
    status              TINYINT     DEFAULT 1 COMMENT '状态 1 启用 0 停用'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '合同包装验收标准';

CREATE TABLE IF NOT EXISTS base_mobile_config (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置 id',
    product_name VARCHAR(100) NOT NULL COMMENT '主营品种',
    sort         INT          DEFAULT 0 COMMENT '排序',
    status       TINYINT      DEFAULT 1 COMMENT '状态 1 启用 0 停用'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '移动端主营品种配置';

-- ===================== V0.4 新增：组织权限域 =====================

CREATE TABLE IF NOT EXISTS org_dict (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '字典 id',
    dict_type VARCHAR(20)  NOT NULL COMMENT '字典类型：org 组织 / position 岗位',
    name      VARCHAR(100) NOT NULL COMMENT '名称',
    parent_id BIGINT       DEFAULT 0 COMMENT '父级 id',
    sort      INT          DEFAULT 0 COMMENT '排序'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '组织/岗位字典';

CREATE TABLE IF NOT EXISTS org_group (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '组 id',
    group_name  VARCHAR(100) NOT NULL COMMENT '组名称',
    owner_id    BIGINT       DEFAULT 0 COMMENT '负责人 id',
    owner_name  VARCHAR(50)  DEFAULT NULL COMMENT '负责人姓名',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '组管理';

CREATE TABLE IF NOT EXISTS org_group_customer (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联 id',
    group_id      BIGINT       DEFAULT 0 COMMENT '组 id',
    customer_id   BIGINT       DEFAULT 0 COMMENT '客户 id',
    customer_name VARCHAR(100) DEFAULT NULL COMMENT '客户名称',
    relation      VARCHAR(20)  DEFAULT NULL COMMENT '关系'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '组内客户（组级共享）';

CREATE TABLE IF NOT EXISTS org_my_customer (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联 id',
    owner_id      BIGINT       DEFAULT 0 COMMENT '负责人 id',
    customer_id   BIGINT       DEFAULT 0 COMMENT '客户 id',
    customer_name VARCHAR(100) DEFAULT NULL COMMENT '客户名称',
    relation      VARCHAR(20)  DEFAULT NULL COMMENT '关系'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '我的客户（个人负责）';

-- ===================== V0.4 新增：采购域 =====================

CREATE TABLE IF NOT EXISTS purchase_supplier_grade (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分级 id',
    supplier_id   BIGINT       DEFAULT 0 COMMENT '供应商 id',
    supplier_name VARCHAR(100) DEFAULT NULL COMMENT '供应商名称',
    grade         VARCHAR(20)  NOT NULL COMMENT '战略/优选/考察/一般',
    remark        VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '供应商分级';

CREATE TABLE IF NOT EXISTS purchase_forecast (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '预案 id',
    plan_type      VARCHAR(10)  NOT NULL COMMENT '年规划 YEAR / 月计划 MONTH / 周优化 WEEK / 日执行 DAY',
    plan_name      VARCHAR(100) DEFAULT NULL COMMENT '预案名称',
    period_start   DATE         DEFAULT NULL COMMENT '周期开始',
    period_end     DATE         DEFAULT NULL COMMENT '周期结束',
    forecast_value DECIMAL(14, 2) DEFAULT 0 COMMENT '预测数量',
    creator        VARCHAR(50)  DEFAULT NULL COMMENT '创建人'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '预测预案（年/月/周/日）';

CREATE TABLE IF NOT EXISTS purchase_inquiry (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '询价 id',
    inquiry_no   VARCHAR(50)  NOT NULL COMMENT '询价单号',
    inquiry_type VARCHAR(20)  DEFAULT NULL COMMENT '急询价 URGENT / 指定询价 SPECIFIED',
    product_name VARCHAR(100) DEFAULT NULL COMMENT '品名',
    product_qty  DECIMAL(14, 2) DEFAULT 0 COMMENT '数量',
    supplier_id  BIGINT       DEFAULT 0 COMMENT '供应商 id',
    supplier_name VARCHAR(100) DEFAULT NULL COMMENT '供应商名称',
    status       VARCHAR(20)  DEFAULT 'CREATED' COMMENT '发起 CREATED → 接收 RECEIVED → 反馈 REPLIED',
    urgent_flag  TINYINT      DEFAULT 0 COMMENT '是否标"急"',
    reply_time   DATETIME     DEFAULT NULL COMMENT '反馈时间',
    creator      VARCHAR(50)  DEFAULT NULL COMMENT '发起人'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '询价管理（急询价/指定询价）';

CREATE TABLE IF NOT EXISTS purchase_apply (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '申请 id',
    apply_no     VARCHAR(50)  NOT NULL COMMENT '采购申请单号',
    inquiry_id   BIGINT       DEFAULT 0 COMMENT '关联询价单 id',
    status       VARCHAR(20)  DEFAULT 'PENDING_APPROVE' COMMENT '待批准 PENDING_APPROVE → 已批准 APPROVED → 待复核 PENDING_REVIEW → 已复核 REVIEWED',
    applicant    VARCHAR(50)  DEFAULT NULL COMMENT '申请人',
    approver     VARCHAR(50)  DEFAULT NULL COMMENT '批准人',
    approve_time DATETIME     DEFAULT NULL COMMENT '批准时间',
    reviewer     VARCHAR(50)  DEFAULT NULL COMMENT '复核人（客服部）',
    review_time  DATETIME     DEFAULT NULL COMMENT '复核时间'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '采购申请审批链（批准→复核）';

CREATE TABLE IF NOT EXISTS purchase_order (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单 id',
    order_no          VARCHAR(50)  NOT NULL COMMENT '订单号',
    source            VARCHAR(20)  DEFAULT NULL COMMENT '来源：供应商活动/销采部/客服部',
    settle_method     VARCHAR(20)  DEFAULT NULL COMMENT '结算方式：现款后货 / 先货后款',
    supplier_id       BIGINT       DEFAULT 0 COMMENT '供应商 id',
    supplier_name     VARCHAR(100) DEFAULT NULL COMMENT '供应商名称',
    product_name      VARCHAR(100) DEFAULT NULL COMMENT '品名',
    qty               DECIMAL(14, 2) DEFAULT 0 COMMENT '数量',
    pay_amount        DECIMAL(14, 2) DEFAULT 0 COMMENT '付款金额',
    status            VARCHAR(20)  DEFAULT 'PENDING_APPROVE' COMMENT '待审批 PENDING_APPROVE → 已审批 APPROVED',
    settlement_status VARCHAR(20)  DEFAULT NULL COMMENT '结算分流：现款后货→待付款 WAIT_PAY→待入库 WAIT_INBOUND；先货后款→待入库 WAIT_INBOUND',
    creator           VARCHAR(50)  DEFAULT NULL COMMENT '创建人',
    create_time       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '待审批订单 + 结算分流';

CREATE TABLE IF NOT EXISTS purchase_debt (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '欠票 id',
    inbound_no    VARCHAR(50)  DEFAULT NULL COMMENT '入库单号',
    inbound_id    BIGINT       DEFAULT 0 COMMENT '入库单 id',
    invoice_no    VARCHAR(50)  DEFAULT NULL COMMENT '发票号码',
    invoice_id    BIGINT       DEFAULT 0 COMMENT '发票 id',
    supplier_id   BIGINT       DEFAULT 0 COMMENT '供应商 id',
    supplier_name VARCHAR(100) DEFAULT NULL COMMENT '供应商名称',
    amount        DECIMAL(14, 2) DEFAULT 0 COMMENT '欠票金额',
    status        VARCHAR(20)  DEFAULT 'OPEN' COMMENT '未开 OPEN / 已开 SETTLED',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '进项欠票（一入库单一欠票）';

CREATE TABLE IF NOT EXISTS purchase_payable (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '应付 id',
    supplier_id   BIGINT       DEFAULT 0 COMMENT '供应商 id',
    supplier_name VARCHAR(100) DEFAULT NULL COMMENT '供应商名称',
    balance       DECIMAL(14, 2) DEFAULT 0 COMMENT '应付余额',
    due_date      DATE         DEFAULT NULL COMMENT '到期日',
    status        VARCHAR(20)  DEFAULT 'OPEN' COMMENT '未付 OPEN / 已付 PAID'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '应付列表';

-- ===================== V0.4 新增：销售域 =====================

CREATE TABLE IF NOT EXISTS sale_order (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单 id',
    order_no     VARCHAR(50)  NOT NULL COMMENT '订单号',
    customer_id  BIGINT       DEFAULT 0 COMMENT '客户 id',
    product_name VARCHAR(100) DEFAULT NULL COMMENT '品名',
    qty          DECIMAL(14, 2) DEFAULT 0 COMMENT '数量',
    amount       DECIMAL(14, 2) DEFAULT 0 COMMENT '金额',
    profit       DECIMAL(14, 2) DEFAULT 0 COMMENT '利润',
    cost         DECIMAL(14, 2) DEFAULT 0 COMMENT '销售成本',
    fee          DECIMAL(14, 2) DEFAULT 0 COMMENT '费用',
    org_id       BIGINT       DEFAULT 0 COMMENT '所属组织 id',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '销售明细/订单';

CREATE TABLE IF NOT EXISTS sale_daily_report (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日报 id',
    report_date  DATE    DEFAULT NULL COMMENT '日期',
    contact_cnt  INT     DEFAULT 0 COMMENT '联系家数',
    lead_cnt     INT     DEFAULT 0 COMMENT '销售线索家数（=报价家数）',
    deal_cnt     INT     DEFAULT 0 COMMENT '成交家数（=开出库单客户数）',
    org_id       BIGINT  DEFAULT 0 COMMENT '所属组织 id'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '业务日报漏斗';

CREATE TABLE IF NOT EXISTS sale_invoice_apply (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '开票申请 id',
    apply_no    VARCHAR(50)  NOT NULL COMMENT '申请单号',
    customer_id BIGINT       DEFAULT 0 COMMENT '客户 id',
    invoice_no  VARCHAR(50)  DEFAULT NULL COMMENT '发票号码',
    status      VARCHAR(20)  DEFAULT 'APPLIED' COMMENT '申请 APPLIED / 待开 PENDING / 已开 ISSUED',
    creator     VARCHAR(50)  DEFAULT NULL COMMENT '申请人'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '开票申请';

-- ===================== V0.4 新增：库存域 =====================

CREATE TABLE IF NOT EXISTS inventory_stock (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '库存 id',
    product_name VARCHAR(100) DEFAULT NULL COMMENT '品名',
    grade        VARCHAR(50)  DEFAULT NULL COMMENT '牌号',
    spec         VARCHAR(100) DEFAULT NULL COMMENT '规格',
    org_id       BIGINT       DEFAULT 0 COMMENT '所属组织 id',
    actual_qty   DECIMAL(14, 2) DEFAULT 0 COMMENT '实际库存',
    transit_qty  DECIMAL(14, 2) DEFAULT 0 COMMENT '在途库存',
    stock_age    INT          DEFAULT 0 COMMENT '库龄（天）',
    age_warn_days INT         DEFAULT 15 COMMENT '库龄预警阈值（默认 15 天）'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '库存统计';

CREATE TABLE IF NOT EXISTS inventory_safe_stock (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '安全库存 id',
    product_name    VARCHAR(100) DEFAULT NULL COMMENT '品名',
    material        VARCHAR(50)  DEFAULT NULL COMMENT '材质',
    org_id          BIGINT       DEFAULT 0 COMMENT '所属组织 id',
    service_level   DECIMAL(5, 2) DEFAULT 0 COMMENT '有货率（%）',
    z_value         DECIMAL(5, 2) DEFAULT 0 COMMENT 'Z 值',
    replenish_cycle INT          DEFAULT 0 COMMENT '补货周期（天）',
    economic_qty    DECIMAL(14, 2) DEFAULT 0 COMMENT '经济补货量',
    order_point_qty DECIMAL(14, 2) DEFAULT 0 COMMENT '订货点量',
    max_qty         DECIMAL(14, 2) DEFAULT 0 COMMENT '最大库存',
    safe_stock      DECIMAL(14, 2) DEFAULT 0 COMMENT '安全库存'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '安全库存设计';

CREATE TABLE IF NOT EXISTS inventory_inbound (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '入库单 id',
    inbound_no     VARCHAR(50)  NOT NULL COMMENT '入库单号',
    inbound_type   VARCHAR(20)  DEFAULT NULL COMMENT '估价/代销/内部',
    source_order_no VARCHAR(50) DEFAULT NULL COMMENT '来源单号',
    product_name   VARCHAR(100) DEFAULT NULL COMMENT '品名',
    qty            DECIMAL(14, 2) DEFAULT 0 COMMENT '数量',
    settle_qty     DECIMAL(14, 2) DEFAULT 0 COMMENT '账面结算数量',
    status         VARCHAR(20)  DEFAULT 'CREATED' COMMENT '制单完成 CREATED → 批准 APPROVED → 保管员审核 CHECKED',
    checker        VARCHAR(50)  DEFAULT NULL COMMENT '审核人',
    audit_level    VARCHAR(20)  DEFAULT NULL COMMENT '分级审核：≤合理称差直接审核 / >合理称差总监/经理审核'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '入库管理';

CREATE TABLE IF NOT EXISTS inventory_outbound (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '出库单 id',
    outbound_no    VARCHAR(50)  NOT NULL COMMENT '出库单号',
    sale_order_no  VARCHAR(50)  DEFAULT NULL COMMENT '销售订单号',
    product_name   VARCHAR(100) DEFAULT NULL COMMENT '品名',
    qty            DECIMAL(14, 2) DEFAULT 0 COMMENT '数量',
    freight_bearer VARCHAR(20)  DEFAULT NULL COMMENT '运费承担方：博宇承担/对方承担',
    carrier        VARCHAR(50)  DEFAULT NULL COMMENT '承运方',
    plate_no       VARCHAR(7)   DEFAULT NULL COMMENT '车牌（≤7 位）',
    driver         VARCHAR(5)   DEFAULT NULL COMMENT '司机（≤5 位）',
    driver_phone   VARCHAR(11)  DEFAULT NULL COMMENT '司机电话（≤11 位）',
    status         VARCHAR(20)  DEFAULT 'CREATED' COMMENT '状态'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '出库管理/发货';

CREATE TABLE IF NOT EXISTS inventory_transfer (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '调拨单 id',
    transfer_no    VARCHAR(50)  NOT NULL COMMENT '调拨单号',
    batch_no       VARCHAR(50)  DEFAULT NULL COMMENT '批次号',
    qty            DECIMAL(14, 2) DEFAULT 0 COMMENT '实提数量',
    target_location VARCHAR(100) DEFAULT NULL COMMENT '目标库位',
    status         VARCHAR(20)  DEFAULT 'CREATED' COMMENT '状态'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '调拨（库位转移）';

CREATE TABLE IF NOT EXISTS inventory_check (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '盘点单 id',
    check_no   VARCHAR(50)  NOT NULL COMMENT '盘点单号',
    batch_no   VARCHAR(50)  DEFAULT NULL COMMENT '批号',
    actual_qty DECIMAL(14, 2) DEFAULT 0 COMMENT '实盘数量',
    status     VARCHAR(20)  DEFAULT 'CREATED' COMMENT '制单批准+保管员审核'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '盘点';

CREATE TABLE IF NOT EXISTS inventory_batch (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '批号 id',
    batch_no     VARCHAR(50)  NOT NULL COMMENT '批号',
    product_name VARCHAR(100) DEFAULT NULL COMMENT '品名',
    create_date  DATE         DEFAULT NULL COMMENT '创建日期（当天唯一）',
    creator      VARCHAR(50)  DEFAULT NULL COMMENT '创建人',
    remark       VARCHAR(255) DEFAULT NULL COMMENT '备注'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '批号管理';

-- ===================== V0.4 新增：财务域 =====================

CREATE TABLE IF NOT EXISTS finance_arrival (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '到账 id',
    account_id   BIGINT       DEFAULT 0 COMMENT '账套 id',
    org_id       BIGINT       DEFAULT 0 COMMENT '所属组织 id',
    amount       DECIMAL(14, 2) DEFAULT 0 COMMENT '到账金额',
    arrival_time DATETIME     DEFAULT NULL COMMENT '到账时间',
    operator     VARCHAR(50)  DEFAULT NULL COMMENT '操作人'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '到账公告';

CREATE TABLE IF NOT EXISTS finance_expense (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '费用 id',
    expense_no      VARCHAR(50)  NOT NULL COMMENT '费用单号',
    customer_id     BIGINT       DEFAULT 0 COMMENT '客户 id',
    product_name    VARCHAR(100) DEFAULT NULL COMMENT '品名',
    amount          DECIMAL(14, 2) DEFAULT 0 COMMENT '费用金额',
    tax_amount      DECIMAL(14, 2) DEFAULT 0 COMMENT '税额',
    allocate_type   VARCHAR(20)  DEFAULT NULL COMMENT '分摊类型',
    allocate_status VARCHAR(20)  DEFAULT 'UNALLOCATED' COMMENT '未分摊 UNALLOCATED / 已分摊 ALLOCATED',
    marked          TINYINT      DEFAULT 0 COMMENT '标记（变色红色）'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '费用管理';

CREATE TABLE IF NOT EXISTS finance_invoice (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '发票 id',
    invoice_no   VARCHAR(50)  NOT NULL COMMENT '发票号码',
    invoice_type VARCHAR(20)  DEFAULT NULL COMMENT '进项/销项',
    customer_id  BIGINT       DEFAULT 0 COMMENT '客户 id',
    product_code VARCHAR(50)  DEFAULT NULL COMMENT '商品编码',
    product_name VARCHAR(100) DEFAULT NULL COMMENT '品名',
    amount       DECIMAL(14, 2) DEFAULT 0 COMMENT '金额',
    status       VARCHAR(20)  DEFAULT 'CREATED' COMMENT '已新增 CREATED / 已审核 APPROVED / 已作废 VOID',
    auditor      VARCHAR(50)  DEFAULT NULL COMMENT '审核人'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '发票管理';

CREATE TABLE IF NOT EXISTS finance_lab_fee (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '化验费 id',
    inbound_id    BIGINT       DEFAULT 0 COMMENT '入库单 id',
    lab_name      VARCHAR(100) DEFAULT NULL COMMENT '化验机构',
    sample_no     VARCHAR(50)  DEFAULT NULL COMMENT '样品编号',
    element       VARCHAR(20)  DEFAULT NULL COMMENT '化验元素',
    lab_fee       DECIMAL(14, 2) DEFAULT 0 COMMENT '化验费',
    report_status VARCHAR(20)  DEFAULT 'PENDING' COMMENT '未上传 PENDING / 合格 PASS / 不合格 FAIL',
    pay_status    VARCHAR(20)  DEFAULT 'UNPAID' COMMENT '未付款 UNPAID / 已付款 PAID / 已冲账 REIMBURSED',
    voucher_no    VARCHAR(50)  DEFAULT NULL COMMENT '凭证号'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '化验费';

CREATE TABLE IF NOT EXISTS finance_ar_ap (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '应收应付 id',
    party_type VARCHAR(10)  DEFAULT NULL COMMENT '客户 CUSTOMER / 供应商 SUPPLIER',
    party_id   BIGINT       DEFAULT 0 COMMENT '往来方 id',
    account_id BIGINT       DEFAULT 0 COMMENT '账套 id',
    org_id     BIGINT       DEFAULT 0 COMMENT '所属组织 id',
    receivable DECIMAL(14, 2) DEFAULT 0 COMMENT '应收',
    payable    DECIMAL(14, 2) DEFAULT 0 COMMENT '应付',
    balance    DECIMAL(14, 2) DEFAULT 0 COMMENT '余额'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '应收应付';

-- ===================== V0.4 新增：CRM =====================

CREATE TABLE IF NOT EXISTS crm_activity (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '活动 id',
    customer_id   BIGINT       DEFAULT 0 COMMENT '客户 id',
    contact_id    BIGINT       DEFAULT 0 COMMENT '联系人 id',
    activity_type VARCHAR(20)  DEFAULT NULL COMMENT '使用/生产/经营',
    relation      VARCHAR(20)  DEFAULT NULL COMMENT '主客/次客/主潜/次潜/大中/主供/次供',
    product_name  VARCHAR(100) DEFAULT NULL COMMENT '品名',
    price         DECIMAL(14, 2) DEFAULT 0 COMMENT '价格',
    pre_need_time DATETIME     DEFAULT NULL COMMENT '预需时间',
    content       TEXT         DEFAULT NULL COMMENT '内容',
    creator       VARCHAR(50)  DEFAULT NULL COMMENT '创建人'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '活动管理';

CREATE TABLE IF NOT EXISTS crm_variety (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '品种资料 id',
    customer_id    BIGINT       DEFAULT 0 COMMENT '客户 id',
    variety_type   VARCHAR(20)  DEFAULT NULL COMMENT '使用/生产/经营',
    product_name   VARCHAR(100) DEFAULT NULL COMMENT '品名',
    grade          VARCHAR(50)  DEFAULT NULL COMMENT '牌号',
    material       VARCHAR(50)  DEFAULT NULL COMMENT '材质',
    spec           VARCHAR(100) DEFAULT NULL COMMENT '规格',
    brand_origin   VARCHAR(100) DEFAULT NULL COMMENT '品牌/产地',
    competitor     VARCHAR(100) DEFAULT NULL COMMENT '竞争对手',
    swot           TEXT         DEFAULT NULL COMMENT 'SWOT',
    monthly_qty    DECIMAL(14, 2) DEFAULT 0 COMMENT '月用量',
    next_month_plan DECIMAL(14, 2) DEFAULT 0 COMMENT '下月计划'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '品种资料';

CREATE TABLE IF NOT EXISTS crm_cert (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '证照 id',
    customer_id        BIGINT       DEFAULT 0 COMMENT '客户 id',
    cert_type          VARCHAR(30)  DEFAULT NULL COMMENT '证照类型',
    expire_date        DATE         DEFAULT NULL COMMENT '到期日期',
    registered_capital DECIMAL(14, 2) DEFAULT 0 COMMENT '注册资本',
    tax_no             VARCHAR(50)  DEFAULT NULL COMMENT '税号',
    verified_flag      TINYINT      DEFAULT 0 COMMENT '资料已核实',
    trade_allowed_flag TINYINT      DEFAULT 0 COMMENT '是否允许交易'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '证照风控';

-- ===================== V0.4 新增：流程引擎 =====================

CREATE TABLE IF NOT EXISTS flow_x5_instance (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '实例 id',
    flow_no      VARCHAR(50)  NOT NULL COMMENT '流程单号',
    flow_type    VARCHAR(20)  DEFAULT NULL COMMENT '报销/借款/付款/退款',
    title        VARCHAR(200) DEFAULT NULL COMMENT '标题',
    amount       DECIMAL(14, 2) DEFAULT 0 COMMENT '金额',
    applicant    VARCHAR(50)  DEFAULT NULL COMMENT '申请人',
    current_step VARCHAR(50)  DEFAULT NULL COMMENT '当前步骤',
    approver     VARCHAR(50)  DEFAULT NULL COMMENT '当前审批人',
    status       VARCHAR(20)  DEFAULT 'RUNNING' COMMENT '运行中 RUNNING / 完成 DONE / 驳回 REJECTED',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT 'X5 流程实例';

CREATE TABLE IF NOT EXISTS flow_anma_instance (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '实例 id',
    flow_no        VARCHAR(50)  NOT NULL COMMENT '流程单号',
    flow_type      VARCHAR(20)  DEFAULT NULL COMMENT '合同/财务审批',
    title          VARCHAR(200) DEFAULT NULL COMMENT '标题',
    contract_amount DECIMAL(14, 2) DEFAULT 0 COMMENT '合同金额',
    supplier_id    BIGINT       DEFAULT 0 COMMENT '供应商 id',
    customer_id    BIGINT       DEFAULT 0 COMMENT '客户 id',
    current_step   VARCHAR(50)  DEFAULT NULL COMMENT '当前步骤',
    approver       VARCHAR(50)  DEFAULT NULL COMMENT '当前审批人',
    status         VARCHAR(20)  DEFAULT 'RUNNING' COMMENT '运行中 RUNNING / 完成 DONE / 驳回 REJECTED'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '安码流程实例';

CREATE TABLE IF NOT EXISTS flow_task (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '任务 id',
    instance_id BIGINT       DEFAULT 0 COMMENT '流程实例 id',
    step_name   VARCHAR(50)  DEFAULT NULL COMMENT '步骤名称',
    assignee    VARCHAR(50)  DEFAULT NULL COMMENT '办理人',
    status      VARCHAR(20)  DEFAULT 'PENDING' COMMENT '待办 PENDING / 已办 DONE',
    remark      VARCHAR(255) DEFAULT NULL COMMENT '备注'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '流程待办/已办';

-- ===================== V0.4 新增：待办事宜 =====================

CREATE TABLE IF NOT EXISTS todo_subscription (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订阅 id',
    board_type VARCHAR(20)  DEFAULT NULL COMMENT 'CRM/采购/销售/财务',
    sub_type   VARCHAR(50)  DEFAULT NULL COMMENT '订阅类型',
    config_json JSON        DEFAULT NULL COMMENT '配置（阀值）',
    owner_id   BIGINT       DEFAULT 0 COMMENT '订阅人 id',
    enabled    TINYINT      DEFAULT 1 COMMENT '启用'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '四板块订阅';

CREATE TABLE IF NOT EXISTS todo_personal (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '待办 id',
    user_id       BIGINT       DEFAULT 0 COMMENT '用户 id',
    todo_type     VARCHAR(20)  DEFAULT NULL COMMENT '待办类型：公共/指派',
    template_type VARCHAR(20)  DEFAULT NULL COMMENT '出库/入库模板',
    remind_time   DATETIME     DEFAULT NULL COMMENT '提醒时间',
    assignee      VARCHAR(50)  DEFAULT NULL COMMENT '指派人员',
    status        VARCHAR(20)  DEFAULT 'PENDING' COMMENT '待办 PENDING / 已办 DONE'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '个人待办';

-- ===================== 决策演示审计 =====================

CREATE TABLE IF NOT EXISTS demo_event_log (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志 id',
    event_type   VARCHAR(50)  DEFAULT NULL COMMENT '事件类型',
    duration     INT          DEFAULT 0 COMMENT '持续天数',
    trigger_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '触发时间'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '演示审计';
