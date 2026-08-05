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
(2, 1, '电解铜', '1#', NULL, NULL, NULL, 1, 1, 1),
(3, 1, '电解铜', '1#', 'Cu', NULL, NULL, 2, 1, 1),
(4, 1, '电解铜', NULL, NULL, '1吨/捆', NULL, 1, 2, 1),
(5, 1, '电解铜', NULL, NULL, NULL, '北方铜业', 1, 3, 1),
(6, 1, '电解锌', NULL, NULL, NULL, NULL, 0, 2, 1),
(7, 1, '电解锌', '0#', 'Zn', NULL, NULL, 6, 1, 1);

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
(1, 1, 1, '苏州应用工厂', '主客'),
(2, 2, 2, '广州深加工基地', '主客');

INSERT IGNORE INTO org_my_customer (id, owner_id, customer_id, customer_name, relation) VALUES
(1, 1, 1, '苏州应用工厂', '主客'),
(2, 1, 2, '广州深加工基地', '次客');

-- ---------------- 供应商分级（purchase_supplier_grade） ----------------
INSERT IGNORE INTO purchase_supplier_grade (id, supplier_id, supplier_name, grade, remark) VALUES
(1, 1, '北方铜业', '战略', '核心供应商，决策演示缺货事件来源'),
(2, 2, '中原铜业', '优选', '备用应急产能'),
(3, 3, '南方铜业', '考察', '潜在替代供应商');

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
(2, 'PO-20260804-002', '客服部', '先货后款', 1, '北方铜业', '电解锌', 800.00, 960000.00, 'APPROVED', 'WAIT_INBOUND', '李采购');

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
(1, 'INV-20260804-001', '进项', 0, 'CU-1-50', '电解铜', 1560000.00, 'APPROVED', '财务王');

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
