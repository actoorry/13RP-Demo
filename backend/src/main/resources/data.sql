-- =====================================================================
-- 13RP-Demo 种子数据（V0.4 九大业务域）
-- 幂等：全部 INSERT IGNORE + 显式主键，可重复执行
-- admin 密码：123456（BCrypt $2a$10$fqVZLuvrQysqb21526Cwce08n5JPfpyplHRSNNV4pj/y2DvekgmxO）
-- =====================================================================

-- ---------------- 机构（sys_institution） ----------------
INSERT IGNORE INTO sys_institution (id, parent_id, name, sort, deleted) VALUES
(1, 0, '博宇集团', 1, 0);

-- ---------------- 人员 / 登录账号（sys_person） ----------------
-- admin/123456，BCrypt 加密，密码不得明文存储
INSERT IGNORE INTO sys_person (id, institution_id, account, password, name, phone, dept, position, status) VALUES
(1, 1, 'admin', '$2a$10$fqVZLuvrQysqb21526Cwce08n5JPfpyplHRSNNV4pj/y2DvekgmxO', '系统管理员', '13800000000', '总裁办', '董事长', 1);

-- ---------------- 角色（sys_role） ----------------
INSERT IGNORE INTO sys_role (id, name, sort, deleted) VALUES
(1, '系统管理', 1, 0),
(2, '组长', 2, 0),
(3, '组员', 3, 0);

-- ---------------- 菜单 / 按钮权限（sys_menu） ----------------
-- 九域导航（MENU）
INSERT IGNORE INTO sys_menu (id, parent_id, name, type, path, perms, sort) VALUES
(1, 0, '基础数据', 'MENU', '/admin/base', NULL, 1),
(2, 0, '组织权限', 'MENU', '/admin/org', NULL, 2),
(3, 0, '采购', 'MENU', '/admin/purchase', NULL, 3),
(4, 0, '销售', 'MENU', '/admin/sale', NULL, 4),
(5, 0, '库存', 'MENU', '/admin/inventory', NULL, 5),
(6, 0, '财务', 'MENU', '/admin/finance', NULL, 6),
(7, 0, 'CRM', 'MENU', '/admin/crm', NULL, 7),
(8, 0, '流程引擎', 'MENU', '/admin/flow', NULL, 8),
(9, 0, '待办事宜', 'MENU', '/admin/todo', NULL, 9);
-- 基础数据按钮
INSERT IGNORE INTO sys_menu (id, parent_id, name, type, path, perms, sort) VALUES
(10, 1, '账套新增', 'BUTTON', NULL, 'base:account:add', 1),
(11, 1, '账套编辑', 'BUTTON', NULL, 'base:account:edit', 2),
(12, 1, '账套删除', 'BUTTON', NULL, 'base:account:delete', 3),
(13, 1, '产品新增', 'BUTTON', NULL, 'base:product:add', 4),
(14, 1, '产品编辑', 'BUTTON', NULL, 'base:product:edit', 5),
(15, 1, '产品作废', 'BUTTON', NULL, 'base:product:delete', 6),
(16, 1, '材质元素新增', 'BUTTON', NULL, 'base:material-element:add', 7),
(17, 1, '包装标准新增', 'BUTTON', NULL, 'base:package-standard:add', 8),
(18, 1, '移动配置新增', 'BUTTON', NULL, 'base:mobile-config:add', 9);
-- 组织权限按钮
INSERT IGNORE INTO sys_menu (id, parent_id, name, type, path, perms, sort) VALUES
(19, 2, '字典新增', 'BUTTON', NULL, 'org:dict:add', 1),
(20, 2, '组新增', 'BUTTON', NULL, 'org:group:add', 2),
(21, 2, '员工新增', 'BUTTON', NULL, 'org:person:add', 3),
(22, 2, '员工编辑', 'BUTTON', NULL, 'org:person:edit', 4),
(23, 2, '划拨/迁移', 'BUTTON', NULL, 'org:group:transfer', 5);
-- 采购域按钮
INSERT IGNORE INTO sys_menu (id, parent_id, name, type, path, perms, sort) VALUES
(24, 3, '供应商分级新增', 'BUTTON', NULL, 'purchase:supplier-grade:add', 1),
(25, 3, '预测预案新增', 'BUTTON', NULL, 'purchase:forecast:add', 2),
(26, 3, '询价新增', 'BUTTON', NULL, 'purchase:inquiry:add', 3),
(27, 3, '采购申请批准', 'BUTTON', NULL, 'purchase:apply:approve', 4),
(28, 3, '采购申请复核', 'BUTTON', NULL, 'purchase:apply:review', 5),
(29, 3, '订单审批', 'BUTTON', NULL, 'purchase:order:approve', 6),
(30, 3, '欠票新增', 'BUTTON', NULL, 'purchase:debt:add', 7),
(31, 3, '应付新增', 'BUTTON', NULL, 'purchase:payable:add', 8);
-- 补充权限码（T8+T9 审查：#6 @PreAuthorize 缺失的 update/delete/流转权限）
-- 基础数据域补全
INSERT IGNORE INTO sys_menu (id, parent_id, name, type, path, perms, sort) VALUES
(32, 1, '材质元素编辑', 'BUTTON', NULL, 'base:material-element:update', 10),
(33, 1, '材质元素删除', 'BUTTON', NULL, 'base:material-element:delete', 11),
(34, 1, '包装标准编辑', 'BUTTON', NULL, 'base:package-standard:update', 12),
(35, 1, '包装标准删除', 'BUTTON', NULL, 'base:package-standard:delete', 13),
(36, 1, '移动配置编辑', 'BUTTON', NULL, 'base:mobile-config:update', 14),
(37, 1, '移动配置删除', 'BUTTON', NULL, 'base:mobile-config:delete', 15);
-- 组织权限域补全
INSERT IGNORE INTO sys_menu (id, parent_id, name, type, path, perms, sort) VALUES
(38, 2, '字典编辑', 'BUTTON', NULL, 'org:dict:update', 6),
(39, 2, '字典删除', 'BUTTON', NULL, 'org:dict:delete', 7),
(40, 2, '组编辑', 'BUTTON', NULL, 'org:group:update', 8),
(41, 2, '权限刷新', 'BUTTON', NULL, 'org:permission:update', 9);
-- 采购域补全
INSERT IGNORE INTO sys_menu (id, parent_id, name, type, path, perms, sort) VALUES
(42, 3, '供应商分级编辑', 'BUTTON', NULL, 'purchase:supplier-grade:update', 9),
(43, 3, '供应商分级删除', 'BUTTON', NULL, 'purchase:supplier-grade:delete', 10),
(44, 3, '预测预案编辑', 'BUTTON', NULL, 'purchase:forecast:update', 11),
(45, 3, '预测预案删除', 'BUTTON', NULL, 'purchase:forecast:delete', 12),
(46, 3, '询价流转', 'BUTTON', NULL, 'purchase:inquiry:update', 13),
(47, 3, '采购申请新增', 'BUTTON', NULL, 'purchase:apply:add', 14),
(48, 3, '订单新增', 'BUTTON', NULL, 'purchase:order:add', 15),
(49, 3, '订单付款', 'BUTTON', NULL, 'purchase:order:pay', 16),
(50, 3, '欠票编辑', 'BUTTON', NULL, 'purchase:debt:update', 17),
(51, 3, '应付编辑', 'BUTTON', NULL, 'purchase:payable:update', 18);

-- ---------------- 角色-菜单（sys_role_menu） ----------------
-- 系统管理：全部菜单 + 全部按钮
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9),
(1, 10), (1, 11), (1, 12), (1, 13), (1, 14), (1, 15), (1, 16), (1, 17), (1, 18),
(1, 19), (1, 20), (1, 21), (1, 22), (1, 23),
(1, 24), (1, 25), (1, 26), (1, 27), (1, 28), (1, 29), (1, 30), (1, 31);
-- 组长：基础/组织/采购 菜单 + 操作按钮（不含删除/作废）
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(2, 1), (2, 2), (2, 3),
(2, 10), (2, 11), (2, 13), (2, 14), (2, 16), (2, 17), (2, 18),
(2, 19), (2, 20), (2, 21), (2, 22), (2, 23),
(2, 24), (2, 25), (2, 26), (2, 27), (2, 28), (2, 29), (2, 30), (2, 31);
-- 组员：仅 基础/组织/采购 菜单（只读）
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(3, 1), (3, 2), (3, 3);
-- 补充权限码角色关联：系统管理（admin）拥有全部新权限；组长含编辑/流转（不含删除/作废）
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(1, 32), (1, 33), (1, 34), (1, 35), (1, 36), (1, 37),
(1, 38), (1, 39), (1, 40), (1, 41),
(1, 42), (1, 43), (1, 44), (1, 45), (1, 46), (1, 47), (1, 48), (1, 49), (1, 50), (1, 51),
(2, 32), (2, 34), (2, 36),
(2, 38), (2, 40), (2, 41),
(2, 42), (2, 44), (2, 46), (2, 47), (2, 48), (2, 49), (2, 50), (2, 51);

-- ---------------- 人员-角色（sys_person_role） ----------------
INSERT IGNORE INTO sys_person_role (person_id, role_id) VALUES
(1, 1);

-- ---------------- 账套（base_account） ----------------
INSERT IGNORE INTO base_account (id, name, code, status, remark) VALUES
(1, '博宇股份', 'BY-GF', 1, '主板上市主体'),
(2, '藏博', 'BZ-CB', 1, '西藏博宇'),
(3, '沈博', 'BZ-SB', 1, '沈阳博宇'),
(4, '总部', 'BZ-ZB', 1, '集团总部');

-- ---------------- 产品主数据（base_product：品名→牌号→材质树） ----------------
INSERT IGNORE INTO base_product (id, account_id, name, grade, material, spec, brand_origin, parent_id, sort, status) VALUES
(1, 1, '电解铜', NULL, NULL, NULL, NULL, 0, 1, 1),
(2, 1, '1#', '1#', NULL, NULL, NULL, 1, 1, 1),
(3, 1, 'Cu', '1#', 'Cu', NULL, NULL, 2, 1, 1),
(4, 1, '电解铜', NULL, NULL, '1吨/捆', NULL, 1, 2, 1),
(5, 1, '电解铜', NULL, NULL, NULL, '北方铜业', 1, 3, 1),
(6, 1, '电解锌', NULL, NULL, NULL, NULL, 0, 2, 1),
(7, 1, 'Zn', '0#', 'Zn', NULL, NULL, 6, 1, 1),
(30, 1, '电解铝', NULL, NULL, NULL, NULL, 0, 3, 1),
(31, 1, '电解镍', NULL, NULL, NULL, NULL, 0, 4, 1),
(32, 1, '电解锡', NULL, NULL, NULL, NULL, 0, 5, 1),
(33, 1, '电解铅', NULL, NULL, NULL, NULL, 0, 6, 1),
(34, 1, '阴极铜', NULL, NULL, NULL, NULL, 0, 7, 1),
(35, 1, '粗铜', NULL, NULL, NULL, NULL, 0, 8, 1),
(36, 1, '铜杆', NULL, NULL, NULL, NULL, 0, 9, 1),
(37, 1, '锌锭', NULL, NULL, NULL, NULL, 0, 10, 1),
(38, 1, '铝锭', NULL, NULL, NULL, NULL, 0, 11, 1),
(39, 1, '黄铜板', NULL, NULL, NULL, NULL, 0, 12, 1),
(40, 1, 'A00', 'A00', NULL, NULL, NULL, 30, 1, 1),
(41, 1, 'Ni9990', 'Ni9990', NULL, NULL, NULL, 31, 1, 1),
(42, 1, 'Sn99.90', 'Sn99.90', NULL, NULL, NULL, 32, 1, 1),
(43, 1, 'Pb99.994', 'Pb99.994', NULL, NULL, NULL, 33, 1, 1),
(44, 1, 'Cu-CATH-1', 'Cu-CATH-1', NULL, NULL, NULL, 34, 1, 1),
(45, 1, 'Cu99.0', 'Cu99.0', NULL, NULL, NULL, 35, 1, 1),
(46, 1, 'TU1', 'TU1', NULL, NULL, NULL, 36, 1, 1),
(47, 1, 'Zn99.995', 'Zn99.995', NULL, NULL, NULL, 37, 1, 1),
(48, 1, 'A199.70', 'A199.70', NULL, NULL, NULL, 38, 1, 1),
(49, 1, 'H62', 'H62', NULL, NULL, NULL, 39, 1, 1),
(50, 1, 'Al', 'A00', 'Al', NULL, NULL, 40, 1, 1),
(51, 1, 'Ni', 'Ni9990', 'Ni', NULL, NULL, 41, 1, 1),
(52, 1, 'Sn', 'Sn99.90', 'Sn', NULL, NULL, 42, 1, 1),
(53, 1, 'Pb', 'Pb99.994', 'Pb', NULL, NULL, 43, 1, 1),
(54, 1, 'Cu', 'Cu-CATH-1', 'Cu', NULL, NULL, 44, 1, 1),
(55, 1, 'Cu', 'Cu99.0', 'Cu', NULL, NULL, 45, 1, 1),
(56, 1, 'Cu', 'TU1', 'Cu', NULL, NULL, 46, 1, 1),
(57, 1, 'Zn', 'Zn99.995', 'Zn', NULL, NULL, 47, 1, 1),
(58, 1, 'Al', 'A199.70', 'Al', NULL, NULL, 48, 1, 1),
(59, 1, 'CuZn', 'H62', 'CuZn', NULL, NULL, 49, 1, 1);

-- ---------------- 材质元素（base_material_element） ----------------
INSERT IGNORE INTO base_material_element (id, symbol, sort, common_value, range_min, range_max, grade_independent) VALUES
(1, 'Cu', 1, '铜', 0.00, 99.00, 0),
(2, 'Zn', 2, '锌', 0.00, 99.00, 0),
(3, 'Al', 3, '铝', 0.00, 99.00, 0),
(4, 'Fe', 4, '铁', 0.00, 99.00, 0);

-- ---------------- 合同包装验收标准（base_package_standard） ----------------
INSERT IGNORE INTO base_package_standard (id, package_name, damage_compensation, status) VALUES
(1, '袋装', '破损按货值 10 倍赔偿', 1),
(2, '纸箱', '破损按货值 5 倍赔偿', 1),
(3, '托盘', '破损按货值 2 倍赔偿', 1);

-- ---------------- 移动端主营品种（base_mobile_config） ----------------
INSERT IGNORE INTO base_mobile_config (id, product_name, sort, status) VALUES
(1, '电解铜', 1, 1),
(2, '电解锌', 2, 1);

-- ---------------- 组织/岗位字典（org_dict） ----------------
INSERT IGNORE INTO org_dict (id, dict_type, name, parent_id, sort) VALUES
(1, 'org', '博宇集团', 0, 1),
(2, 'org', '销采服务部', 1, 2),
(3, 'org', '各地销售部', 1, 3),
(4, 'org', '品种销采部', 1, 4),
(5, 'org', '共享财务部', 1, 5),
(6, 'org', '智慧数据部', 1, 6),
(7, 'org', '监察审计部', 1, 7),
(8, 'org', '人资合作部', 1, 8),
(9, 'org', '卓越流程部', 1, 9),
(10, 'org', '总裁办', 1, 10),
(11, 'org', '幸福盟事业部', 1, 11),
(101, 'position', '董事长', 0, 1),
(102, 'position', '营销总监', 0, 2),
(103, 'position', '销采总经理', 0, 3),
(104, 'position', '区域经理', 0, 4),
(105, 'position', '销售经理', 0, 5),
(106, 'position', '销售副经理', 0, 6),
(107, 'position', '销售主管', 0, 7),
(108, 'position', '销售内勤', 0, 8);

-- ---------------- 组管理（org_group / 组内客户 / 我的客户） ----------------
INSERT IGNORE INTO org_group (id, group_name, owner_id, owner_name) VALUES
(1, '华东销售一组', 1, '王经理'),
(2, '华南销售二组', 1, '李经理');

INSERT IGNORE INTO org_group_customer (id, group_id, customer_id, customer_name, relation) VALUES
(1, 1, 1, '沈阳应用工厂', '主客'),
(2, 2, 2, '营口深加工基地', '主客');

INSERT IGNORE INTO org_my_customer (id, owner_id, customer_id, customer_name, relation) VALUES
(1, 1, 1, '沈阳应用工厂', '主客'),
(2, 1, 2, '营口深加工基地', '次客');

-- ---------------- 供应商分级（purchase_supplier_grade） ----------------
INSERT IGNORE INTO purchase_supplier_grade (id, supplier_id, supplier_name, grade, remark) VALUES
(1, 1, '北方铜业', '战略', '核心供应商，决策演示缺货事件来源'),
(2, 2, '中原铜业', '优选', '备用应急产能'),
(3, 3, '南方铜业', '考察', '潜在替代供应商'),
(4, 4, '营口有色金属', '考察', '新引入供应商'),
(5, 5, '沈阳铜业加工', '优选', '区域供应商');

-- ---------------- 预测预案（purchase_forecast） ----------------
INSERT IGNORE INTO purchase_forecast (id, plan_type, plan_name, period_start, period_end, forecast_value, creator) VALUES
(1, 'YEAR', '2026 年电解铜采购规划', '2026-01-01', '2026-12-31', 36000.00, '李采购'),
(2, 'MONTH', '2026 年 8 月电解铜采购计划', '2026-08-01', '2026-08-31', 3000.00, '李采购'),
(3, 'WEEK', '8 月第 2 周电解铜优化', '2026-08-10', '2026-08-16', 800.00, '李采购'),
(4, 'DAY', '08-04 日电解铜执行', '2026-08-04', '2026-08-04', 120.00, '李采购');

-- ---------------- 询价（purchase_inquiry） ----------------
INSERT IGNORE INTO purchase_inquiry (id, inquiry_no, inquiry_type, product_name, product_qty, supplier_id, supplier_name, status, urgent_flag, reply_time, creator) VALUES
(1, 'INQ-20260804-001', 'URGENT', '电解铜', 1200.00, 2, '中原铜业', 'REPLIED', 1, '2026-08-04 10:30:00', '李采购'),
(2, 'INQ-20260804-002', 'SPECIFIED', '电解锌', 800.00, 1, '北方铜业', 'RECEIVED', 0, NULL, '李采购');

-- ---------------- 采购申请（purchase_apply：批准→复核两段审批） ----------------
INSERT IGNORE INTO purchase_apply (id, apply_no, inquiry_id, status, applicant, approver, approve_time, reviewer, review_time) VALUES
(1, 'PA-20260804-001', 1, 'REVIEWED', '李采购', '王经理', '2026-08-04 11:00:00', '客服部赵', '2026-08-04 11:30:00'),
(2, 'PA-20260804-002', 2, 'APPROVED', '李采购', '王经理', '2026-08-04 11:10:00', NULL, NULL);

-- ---------------- 待审批订单 + 结算分流（purchase_order） ----------------
-- 现款后货 → WAIT_PAY（待付款）；先货后款 → WAIT_INBOUND（待入库）
INSERT IGNORE INTO purchase_order (id, order_no, source, settle_method, supplier_id, supplier_name, product_name, qty, pay_amount, status, settlement_status, creator) VALUES
(1, 'PO-20260804-001', '销采部', '现款后货', 2, '中原铜业', '电解铜', 1200.00, 1560000.00, 'APPROVED', 'WAIT_PAY', '李采购'),
(2, 'PO-20260804-002', '客服部', '先货后款', 1, '北方铜业', '电解锌', 800.00, 960000.00, 'APPROVED', 'WAIT_INBOUND', '李采购'),
(7, 'PO-20260810-101', '销采部', '现款后货', 1, '北方铜业', '电解铝', 200.00, 520000.00, 'PENDING_APPROVE', 'WAIT_INBOUND', '李采购'),
(8, 'PO-20260810-102', '销采部', '先货后款', 2, '中原铜业', '电解镍', 50.00, 750000.00, 'APPROVED', 'WAIT_PAY', '李采购'),
(9, 'PO-20260810-103', '客服部', '现款后货', 3, '南方铜业', '电解锡', 30.00, 720000.00, 'PENDING_APPROVE', 'WAIT_INBOUND', '李采购');

-- ---------------- 进项欠票（purchase_debt） ----------------
INSERT IGNORE INTO purchase_debt (id, inbound_no, inbound_id, invoice_no, invoice_id, supplier_id, supplier_name, amount, status) VALUES
(1, 'IN-20260804-001', 1, NULL, NULL, 2, '中原铜业', 156000.00, 'OPEN');

-- ---------------- 应付列表（purchase_payable） ----------------
INSERT IGNORE INTO purchase_payable (id, supplier_id, supplier_name, balance, due_date, status) VALUES
(1, 2, '中原铜业', 1560000.00, '2026-09-04', 'OPEN'),
(2, 1, '北方铜业', 960000.00, '2026-08-20', 'OPEN');

-- ---------------- 销售域示例（sale_order） ----------------
INSERT IGNORE INTO sale_order (id, order_no, customer_id, product_name, qty, amount, profit, cost, fee, org_id) VALUES
(1, 'SO-20260804-001', 1, '电解铜', 1200.00, 1560000.00, 300000.00, 1200000.00, 8000.00, 1);

-- ---------------- 库存域示例（inventory_stock） ----------------
INSERT IGNORE INTO inventory_stock (id, product_name, grade, spec, org_id, actual_qty, transit_qty, stock_age, age_warn_days) VALUES
(1, '电解铜', '1#', '1吨/捆', 1, 500.00, 1200.00, 10, 15);

-- ---------------- 财务域示例（finance_arrival / finance_invoice） ----------------
INSERT IGNORE INTO finance_arrival (id, account_id, org_id, amount, arrival_time, operator) VALUES
(1, 1, 1, 500000.00, '2026-08-04 09:00:00', '财务李');

INSERT IGNORE INTO finance_invoice (id, invoice_no, invoice_type, customer_id, product_code, product_name, amount, status, auditor) VALUES
(1, 'INV-20260804-001', '进项', 0, 'CU-1-50', '电解铜', 1560000.00, 'APPROVED', '财务王'),
(2, 'INV-20260810-001', '进项', 1, NULL, '电解铝', 520000.00, 'CREATED', NULL),
(3, 'INV-20260810-002', '销项', 2, NULL, '电解镍', 750000.00, 'APPROVED', '李采购'),
(4, 'INV-20260810-003', '进项', 1, NULL, '电解锡', 720000.00, 'APPROVED', '王财务');

-- ---------------- CRM 示例（crm_activity） ----------------
INSERT IGNORE INTO crm_activity (id, customer_id, contact_id, activity_type, relation, product_name, price, pre_need_time, content, creator) VALUES
(1, 1, 1, '使用', '主客', '电解铜', 1300.00, '2026-08-10 00:00:00', '客户询价，需要预需提醒', '李采购');

-- ---------------- 流程引擎示例（flow_x5_instance / flow_task） ----------------
INSERT IGNORE INTO flow_x5_instance (id, flow_no, flow_type, title, amount, applicant, current_step, approver, status) VALUES
(1, 'X5-20260804-001', '报销', '差旅费报销', 1200.00, '李采购', '审批中', '王经理', 'RUNNING');

INSERT IGNORE INTO flow_task (id, instance_id, step_name, assignee, status, remark) VALUES
(1, 1, '部门审批', '王经理', 'PENDING', '报销金额 1200 元');

-- ---------------- 待办事宜示例（todo_subscription / todo_personal） ----------------
INSERT IGNORE INTO todo_subscription (id, board_type, sub_type, config_json, owner_id, enabled) VALUES
(1, '采购', '进项欠票', JSON_OBJECT('amount', 10000, 'days', 7), 1, 1),
(2, '销售', '应收账款', JSON_OBJECT('balance', 50000, 'days', 7), 1, 1);

INSERT IGNORE INTO todo_personal (id, user_id, todo_type, template_type, remind_time, assignee, status) VALUES
(1, 1, '指派', '出库模板', '2026-08-05 09:00:00', '李采购', 'PENDING');

-- =====================================================================
-- T10 后端 A 追加（销售域 + 库存域种子 / 六域补全种子 / 按钮权限码）
-- 幂等：全部 INSERT IGNORE + 显式主键；域级菜单 id 4/5 已存在，不重复插入
-- =====================================================================

-- ---------------- 销售域种子补全（sale_order 已有 id=1，追加 2/3） ----------------
INSERT IGNORE INTO sale_order (id, order_no, customer_id, product_name, qty, amount, profit, cost, fee, org_id) VALUES
(2, 'SO-20260804-002', 2, '电解锌', 800.00, 960000.00, 180000.00, 720000.00, 5000.00, 1),
(3, 'SO-20260805-001', 1, '电解铜', 1500.00, 1950000.00, 360000.00, 1500000.00, 9000.00, 1);

-- ---------------- 业务日报（sale_daily_report） ----------------
INSERT IGNORE INTO sale_daily_report (id, report_date, contact_cnt, lead_cnt, deal_cnt, org_id) VALUES
(1, '2026-08-04', 20, 8, 3, 1),
(2, '2026-08-05', 25, 10, 4, 1),
(3, '2026-08-06', 22, 9, 3, 0),
(4, '2026-08-07', 28, 12, 5, 0),
(5, '2026-08-08', 26, 11, 4, 0),
(6, '2026-08-09', 32, 15, 6, 0),
(7, '2026-08-10', 30, 14, 7, 0);

-- ---------------- 开票申请（sale_invoice_apply：APPLIED→PENDING→ISSUED） ----------------
INSERT IGNORE INTO sale_invoice_apply (id, apply_no, customer_id, invoice_no, status, creator) VALUES
(1, 'SI-20260804-001', 1, 'INV-20260804-002', 'ISSUED', '陈销售'),
(2, 'SI-20260804-002', 2, NULL, 'PENDING', '陈销售'),
(3, 'SI-20260805-001', 1, NULL, 'APPLIED', '陈销售');

-- ---------------- 库存统计补全（inventory_stock 已有 id=1，追加 2/3） ----------------
-- 电解锌库龄 20 天 ≥ 15 → 红色预警；电解铜 6 天不预警
INSERT IGNORE INTO inventory_stock (id, product_name, grade, spec, org_id, actual_qty, transit_qty, stock_age, age_warn_days) VALUES
(2, '电解锌', '0#', '1吨/捆', 1, 300.00, 0.00, 20, 15),
(3, '电解铜', '1#', '1吨/捆', 1, 800.00, 0.00, 6, 15),
(4, '电解铝', 'A00', NULL, 0, 650.00, 0.00, 8, 15),
(5, '电解镍', 'Ni9990', NULL, 0, 180.00, 0.00, 5, 15),
(6, '电解锡', 'Sn99.90', NULL, 0, 95.00, 0.00, 12, 15),
(7, '铜杆', 'TU1', NULL, 0, 420.00, 200.00, 3, 15),
(8, '铝锭', 'A199.70', NULL, 0, 300.00, 0.00, 18, 15);

-- ---------------- 安全库存设计（inventory_safe_stock） ----------------
INSERT IGNORE INTO inventory_safe_stock (id, product_name, material, org_id, service_level, z_value, replenish_cycle, economic_qty, order_point_qty, max_qty, safe_stock) VALUES
(1, '电解铜', 'Cu', 1, 95.00, 1.65, 7, 600.00, 900.00, 1500.00, 600.00),
(2, '电解锌', 'Zn', 1, 90.00, 1.28, 7, 400.00, 600.00, 1000.00, 400.00);

-- ---------------- 入库管理（inventory_inbound：CREATED→APPROVED→CHECKED） ----------------
INSERT IGNORE INTO inventory_inbound (id, inbound_no, inbound_type, source_order_no, product_name, qty, settle_qty, status, checker, audit_level) VALUES
(1, 'IN-20260804-001', '估价', 'PO-20260804-001', '电解铜', 1200.00, 1180.00, 'CHECKED', '王仓储', '直接审核'),
(2, 'IN-20260805-001', '代销', 'PO-20260804-002', '电解锌', 800.00, 790.00, 'APPROVED', NULL, '总监审核');

-- ---------------- 出库管理/发货（inventory_outbound：CREATED→APPROVED） ----------------
INSERT IGNORE INTO inventory_outbound (id, outbound_no, sale_order_no, product_name, qty, freight_bearer, carrier, plate_no, driver, driver_phone, status) VALUES
(1, 'OUT-20260804-001', 'SO-20260804-001', '电解铜', 500.00, '博宇承担', '中远物流', '苏A1234', '张师傅', '13811112222', 'APPROVED'),
(2, 'OUT-20260805-001', 'SO-20260804-002', '电解锌', 800.00, '对方承担', '顺丰快运', '苏B5678', '李师傅', '13833334444', 'CREATED');

-- ---------------- 调拨（inventory_transfer） ----------------
INSERT IGNORE INTO inventory_transfer (id, transfer_no, batch_no, qty, target_location, status) VALUES
(1, 'TR-20260804-001', 'BATCH-20260804', 200.00, '3号库区A排', 'CREATED'),
(2, 'TR-20260805-001', 'BATCH-20260805', 150.00, '5号库区B排', 'CREATED');

-- ---------------- 盘点（inventory_check：CREATED→APPROVED→CHECKED） ----------------
INSERT IGNORE INTO inventory_check (id, check_no, batch_no, actual_qty, status) VALUES
(1, 'CK-20260804-001', 'BATCH-20260804', 1180.00, 'CHECKED'),
(2, 'CK-20260805-001', 'BATCH-20260805', 790.00, 'CREATED');

-- ---------------- 批号管理（inventory_batch） ----------------
INSERT IGNORE INTO inventory_batch (id, batch_no, product_name, create_date, creator, remark) VALUES
(1, 'BATCH-20260804', '电解铜', '2026-08-04', '王仓储', '当日批次'),
(2, 'BATCH-20260805', '电解锌', '2026-08-05', '王仓储', '当日批次');

-- ---------------- 财务域补全（费用/化验费/应收应付） ----------------
INSERT IGNORE INTO finance_expense (id, expense_no, customer_id, product_name, amount, tax_amount, allocate_type, allocate_status, marked) VALUES
(1, 'FE-20260804-001', 1, '电解铜', 8000.00, 1040.00, '按数量', 'ALLOCATED', 1),
(2, 'FE-20260805-001', 2, '电解锌', 5000.00, 650.00, '按金额', 'UNALLOCATED', 0);

INSERT IGNORE INTO finance_lab_fee (id, inbound_id, lab_name, sample_no, element, lab_fee, report_status, pay_status, voucher_no) VALUES
(1, 1, '博宇检测中心', 'S-20260804-001', 'Cu', 1200.00, 'PASS', 'PAID', 'VCH-20260804-001'),
(2, 2, '博宇检测中心', 'S-20260805-001', 'Zn', 1000.00, 'PENDING', 'UNPAID', NULL),
(3, NULL, '沈阳质检中心', 'S-20260810-003', 'Sn', 1200.00, 'PENDING', 'UNPAID', NULL),
(4, NULL, '沈阳质检中心', 'S-20260810-004', 'Al', 1500.00, 'PASS', 'PAID', NULL),
(5, NULL, '沈阳质检中心', 'S-20260810-005', 'Ni', 1800.00, 'FAIL', 'UNPAID', NULL);

INSERT IGNORE INTO finance_ar_ap (id, party_type, party_id, account_id, org_id, receivable, payable, balance) VALUES
(1, 'CUSTOMER', 1, 1, 1, 1560000.00, 0.00, 1560000.00),
(2, 'SUPPLIER', 2, 1, 1, 0.00, 1560000.00, -1560000.00);

-- ---------------- CRM 补全（品种资料/证照风控/客户/线索） ----------------
INSERT IGNORE INTO crm_variety (id, customer_id, variety_type, product_name, grade, material, spec, brand_origin, competitor, swot, monthly_qty, next_month_plan) VALUES
(1, 1, '使用', '电解铜', '1#', 'Cu', '1吨/捆', '北方铜业', '中原铜业', '客户稳定采购，需关注竞争对手报价', 1200.00, 1500.00),
(2, 2, '生产', '电解锌', '0#', 'Zn', '1吨/捆', '北方铜业', '南方铜业', '自产深加工，需保证来料稳定', 800.00, 900.00);

INSERT IGNORE INTO crm_cert (id, customer_id, cert_type, expire_date, registered_capital, tax_no, verified_flag, trade_allowed_flag) VALUES
(1, 1, '营业执照', '2029-12-31', 10000000.00, '91320500MA1XXXX01', 1, 1),
(2, 2, '营业执照', '2028-06-30', 8000000.00, '91440101MA2YYYY02', 1, 1),
(3, 1, '生产许可证', '2029-06-30', 5000000.00, 'SYSC001', 1, 1),
(4, 2, '质量管理体系认证', '2028-12-31', 6000000.00, 'YKISO001', 1, 1);

INSERT IGNORE INTO crm_customer (id, name, source, company_type, phone, tel, email, address, industry, `level`, owner_id, follow_flag, converted_flag) VALUES
(1, '沈阳应用工厂', '线下展会', '制造', '13811110001', '024-88886666', 'sy@example.com', '沈阳市浑南区', '有色金属加工', 'A', 1, 1, 1),
(2, '营口深加工基地', '转介绍', '制造', '13922220002', '0417-66668888', 'yk@example.com', '营口市鲅鱼圈区', '有色金属深加工', 'B', 1, 1, 1);

INSERT IGNORE INTO crm_lead (id, name, source, company_type, phone, email, industry, `level`, owner_id, follow_flag, converted_flag) VALUES
(1, '宁波压延有限公司', '广告投放', '制造', '13733330003', 'nb@example.com', '压延加工', 'B', 1, 1, 0),
(2, '重庆线缆厂', '客户转介绍', '制造', '13644440004', 'cq@example.com', '线缆制造', 'C', 1, 0, 0);

-- ---------------- 流程引擎补全（安码流程） ----------------
INSERT IGNORE INTO flow_anma_instance (id, flow_no, flow_type, title, contract_amount, supplier_id, customer_id, current_step, approver, status) VALUES
(1, 'ANMA-20260804-001', '合同审批', '电解铜采购框架合同', 3000000.00, 1, NULL, '法务审批', '王经理', 'RUNNING'),
(2, 'ANMA-20260805-001', '财务审批', '货款付款审批', 1560000.00, 2, NULL, '财务复核', '财务王', 'RUNNING');

-- ---------------- 销售域按钮权限码（T10：id 从 52 起，parent_id=4 销售） ----------------
INSERT IGNORE INTO sys_menu (id, parent_id, name, type, path, perms, sort) VALUES
(52, 4, '销售订单新增', 'BUTTON', NULL, 'sale:order:add', 1),
(53, 4, '销售订单编辑', 'BUTTON', NULL, 'sale:order:update', 2),
(54, 4, '销售订单删除', 'BUTTON', NULL, 'sale:order:delete', 3),
(55, 4, '业务日报新增', 'BUTTON', NULL, 'sale:daily-report:add', 4),
(56, 4, '业务日报编辑', 'BUTTON', NULL, 'sale:daily-report:update', 5),
(57, 4, '业务日报删除', 'BUTTON', NULL, 'sale:daily-report:delete', 6),
(58, 4, '开票申请新增', 'BUTTON', NULL, 'sale:invoice-apply:add', 7),
(59, 4, '开票申请流转', 'BUTTON', NULL, 'sale:invoice-apply:update', 8),
(60, 4, '开票申请删除', 'BUTTON', NULL, 'sale:invoice-apply:delete', 9);

-- ---------------- 库存域按钮权限码（parent_id=5 库存） ----------------
INSERT IGNORE INTO sys_menu (id, parent_id, name, type, path, perms, sort) VALUES
(61, 5, '库存新增', 'BUTTON', NULL, 'inventory:stock:add', 1),
(62, 5, '库存编辑', 'BUTTON', NULL, 'inventory:stock:update', 2),
(63, 5, '库存删除', 'BUTTON', NULL, 'inventory:stock:delete', 3),
(64, 5, '安全库存新增', 'BUTTON', NULL, 'inventory:safe-stock:add', 4),
(65, 5, '安全库存编辑', 'BUTTON', NULL, 'inventory:safe-stock:update', 5),
(66, 5, '安全库存删除', 'BUTTON', NULL, 'inventory:safe-stock:delete', 6),
(67, 5, '入库新增', 'BUTTON', NULL, 'inventory:inbound:add', 7),
(68, 5, '入库审批流转', 'BUTTON', NULL, 'inventory:inbound:update', 8),
(69, 5, '入库删除', 'BUTTON', NULL, 'inventory:inbound:delete', 9),
(70, 5, '出库新增', 'BUTTON', NULL, 'inventory:outbound:add', 10),
(71, 5, '出库审批', 'BUTTON', NULL, 'inventory:outbound:update', 11),
(72, 5, '出库删除', 'BUTTON', NULL, 'inventory:outbound:delete', 12),
(73, 5, '调拨新增', 'BUTTON', NULL, 'inventory:transfer:add', 13),
(74, 5, '调拨编辑', 'BUTTON', NULL, 'inventory:transfer:update', 14),
(75, 5, '调拨删除', 'BUTTON', NULL, 'inventory:transfer:delete', 15),
(76, 5, '盘点新增', 'BUTTON', NULL, 'inventory:check:add', 16),
(77, 5, '盘点审批流转', 'BUTTON', NULL, 'inventory:check:update', 17),
(78, 5, '盘点删除', 'BUTTON', NULL, 'inventory:check:delete', 18),
(79, 5, '批号新增', 'BUTTON', NULL, 'inventory:batch:add', 19),
(80, 5, '批号编辑', 'BUTTON', NULL, 'inventory:batch:update', 20),
(81, 5, '批号删除', 'BUTTON', NULL, 'inventory:batch:delete', 21);

-- ---------------- 角色-菜单（admin role_id=1 关联 T10 新按钮） ----------------
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(1, 52), (1, 53), (1, 54), (1, 55), (1, 56), (1, 57), (1, 58), (1, 59), (1, 60),
(1, 61), (1, 62), (1, 63), (1, 64), (1, 65), (1, 66), (1, 67), (1, 68), (1, 69),
(1, 70), (1, 71), (1, 72), (1, 73), (1, 74), (1, 75), (1, 76), (1, 77), (1, 78),
(1, 79), (1, 80), (1, 81);

-- ---------------- 财务域按钮权限码（T11：id 从 82 起，parent_id=6 财务） ----------------
INSERT IGNORE INTO sys_menu (id, parent_id, name, type, path, perms, sort) VALUES
(82, 6, '到账公告新增', 'BUTTON', NULL, 'finance:arrival:add', 1),
(83, 6, '到账公告编辑', 'BUTTON', NULL, 'finance:arrival:update', 2),
(84, 6, '到账公告删除', 'BUTTON', NULL, 'finance:arrival:delete', 3),
(85, 6, '费用新增', 'BUTTON', NULL, 'finance:expense:add', 4),
(86, 6, '费用编辑', 'BUTTON', NULL, 'finance:expense:update', 5),
(87, 6, '费用删除', 'BUTTON', NULL, 'finance:expense:delete', 6),
(88, 6, '发票新增', 'BUTTON', NULL, 'finance:invoice:add', 7),
(89, 6, '发票审核流转', 'BUTTON', NULL, 'finance:invoice:update', 8),
(90, 6, '发票删除', 'BUTTON', NULL, 'finance:invoice:delete', 9),
(91, 6, '化验费新增', 'BUTTON', NULL, 'finance:lab-fee:add', 10),
(92, 6, '化验费流转', 'BUTTON', NULL, 'finance:lab-fee:update', 11),
(93, 6, '化验费删除', 'BUTTON', NULL, 'finance:lab-fee:delete', 12),
(94, 6, '应收应付新增', 'BUTTON', NULL, 'finance:ar-ap:add', 13),
(95, 6, '应收应付编辑', 'BUTTON', NULL, 'finance:ar-ap:update', 14),
(96, 6, '应收应付删除', 'BUTTON', NULL, 'finance:ar-ap:delete', 15);

-- ---------------- CRM 域按钮权限码（parent_id=7 CRM） ----------------
INSERT IGNORE INTO sys_menu (id, parent_id, name, type, path, perms, sort) VALUES
(97, 7, '活动新增', 'BUTTON', NULL, 'crm:activity:add', 1),
(98, 7, '活动编辑', 'BUTTON', NULL, 'crm:activity:update', 2),
(99, 7, '活动删除', 'BUTTON', NULL, 'crm:activity:delete', 3),
(100, 7, '品种资料新增', 'BUTTON', NULL, 'crm:variety:add', 4),
(101, 7, '品种资料编辑', 'BUTTON', NULL, 'crm:variety:update', 5),
(102, 7, '品种资料删除', 'BUTTON', NULL, 'crm:variety:delete', 6),
(103, 7, '证照风控新增', 'BUTTON', NULL, 'crm:cert:add', 7),
(104, 7, '证照风控编辑', 'BUTTON', NULL, 'crm:cert:update', 8),
(105, 7, '证照风控删除', 'BUTTON', NULL, 'crm:cert:delete', 9),
(106, 7, '客户资料新增', 'BUTTON', NULL, 'crm:customer:add', 10),
(107, 7, '客户资料编辑', 'BUTTON', NULL, 'crm:customer:update', 11),
(108, 7, '客户资料删除', 'BUTTON', NULL, 'crm:customer:delete', 12),
(109, 7, '线索新增', 'BUTTON', NULL, 'crm:lead:add', 13),
(110, 7, '线索编辑', 'BUTTON', NULL, 'crm:lead:update', 14),
(111, 7, '线索删除', 'BUTTON', NULL, 'crm:lead:delete', 15);

-- ---------------- 流程引擎域按钮权限码（parent_id=8 流程引擎） ----------------
INSERT IGNORE INTO sys_menu (id, parent_id, name, type, path, perms, sort) VALUES
(112, 8, 'X5 流程新增', 'BUTTON', NULL, 'flow:x5:add', 1),
(113, 8, 'X5 流程审批流转', 'BUTTON', NULL, 'flow:x5:update', 2),
(114, 8, 'X5 流程删除', 'BUTTON', NULL, 'flow:x5:delete', 3),
(115, 8, '安码流程新增', 'BUTTON', NULL, 'flow:anma:add', 4),
(116, 8, '安码流程审批流转', 'BUTTON', NULL, 'flow:anma:update', 5),
(117, 8, '安码流程删除', 'BUTTON', NULL, 'flow:anma:delete', 6),
(118, 8, '流程待办新增', 'BUTTON', NULL, 'flow:task:add', 7),
(119, 8, '流程待办审批', 'BUTTON', NULL, 'flow:task:update', 8),
(120, 8, '流程待办删除', 'BUTTON', NULL, 'flow:task:delete', 9);

-- ---------------- 待办事宜域按钮权限码（parent_id=9 待办事宜） ----------------
INSERT IGNORE INTO sys_menu (id, parent_id, name, type, path, perms, sort) VALUES
(121, 9, '订阅新增', 'BUTTON', NULL, 'todo:subscription:add', 1),
(122, 9, '订阅编辑', 'BUTTON', NULL, 'todo:subscription:update', 2),
(123, 9, '订阅删除', 'BUTTON', NULL, 'todo:subscription:delete', 3),
(124, 9, '个人待办新增', 'BUTTON', NULL, 'todo:personal:add', 4),
(125, 9, '个人待办编辑', 'BUTTON', NULL, 'todo:personal:update', 5),
(126, 9, '个人待办删除', 'BUTTON', NULL, 'todo:personal:delete', 6);

-- ---------------- 角色-菜单（admin role_id=1 关联 T11+T12 新按钮） ----------------
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(1, 82), (1, 83), (1, 84), (1, 85), (1, 86), (1, 87), (1, 88), (1, 89), (1, 90),
(1, 91), (1, 92), (1, 93), (1, 94), (1, 95), (1, 96), (1, 97), (1, 98), (1, 99),
(1, 100), (1, 101), (1, 102), (1, 103), (1, 104), (1, 105), (1, 106), (1, 107), (1, 108),
(1, 109), (1, 110), (1, 111), (1, 112), (1, 113), (1, 114), (1, 115), (1, 116), (1, 117),
(1, 118), (1, 119), (1, 120), (1, 121), (1, 122), (1, 123), (1, 124), (1, 125), (1, 126);

-- ---------------- 采购订单关闭权限码（批次 2 复审 P1-7：id=200 顺延，parent_id=3 采购） ----------------
INSERT IGNORE INTO sys_menu (id, parent_id, name, type, path, perms, sort) VALUES
(200, 3, '订单关闭', 'BUTTON', NULL, 'purchase:order:close', 19);

-- 订单关闭权限码角色关联：系统管理（admin）+ 组长（组长已有订单审批/付款）
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(1, 200), (2, 200);
