package com.boyu.demo.module.org.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.boyu.demo.common.Result;
import com.boyu.demo.module.org.entity.OrgGroup;
import com.boyu.demo.module.org.entity.OrgGroupCustomer;
import com.boyu.demo.module.org.entity.OrgMyCustomer;
import com.boyu.demo.module.org.mapper.OrgGroupCustomerMapper;
import com.boyu.demo.module.org.mapper.OrgGroupMapper;
import com.boyu.demo.module.org.mapper.OrgMyCustomerMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 组管理服务：组内客户（组级共享） vs 我的客户（个人负责）+ 划拨/批量迁移。
 */
@Service
public class GroupService {

    private final OrgGroupMapper groupMapper;
    private final OrgGroupCustomerMapper groupCustomerMapper;
    private final OrgMyCustomerMapper myCustomerMapper;

    public GroupService(OrgGroupMapper groupMapper, OrgGroupCustomerMapper groupCustomerMapper,
                        OrgMyCustomerMapper myCustomerMapper) {
        this.groupMapper = groupMapper;
        this.groupCustomerMapper = groupCustomerMapper;
        this.myCustomerMapper = myCustomerMapper;
    }

    public OrgGroupMapper groupMapper() {
        return groupMapper;
    }

    public OrgGroupCustomerMapper groupCustomerMapper() {
        return groupCustomerMapper;
    }

    public OrgMyCustomerMapper myCustomerMapper() {
        return myCustomerMapper;
    }

    /** 组详情：组内客户 + 我的客户。 */
    public Map<String, Object> detail(Long groupId) {
        Map<String, Object> data = new LinkedHashMap<>();
        List<OrgGroupCustomer> groupCustomers = groupId != null
                ? groupCustomerMapper.selectList(new LambdaQueryWrapper<OrgGroupCustomer>()
                        .eq(OrgGroupCustomer::getGroupId, groupId))
                : List.of();
        data.put("groupCustomers", groupCustomers);
        data.put("myCustomers", myCustomerMapper.selectList(null));
        return data;
    }

    /**
     * 划拨/迁移（契约 §3.4）：
     * <ul>
     *   <li>GROUP → 划拨到组：customerIds 写入 org_group_customer（targetGroupId）</li>
     *   <li>COMPANY → 划拨到公司：customerIds 从组内移除（回公司池）</li>
     *   <li>OWNER → 批量迁移主要负责人：customerIds 的负责人改为 targetOwnerId（org_my_customer）</li>
     * </ul>
     */
    @Transactional
    public Result<Void> transfer(Map<String, Object> body) {
        // 兼容前端 transferType（company/group/owner）与 action（COMPANY/GROUP/OWNER）两套字段名
        String action = str(body, "action");
        if (action == null || action.isBlank()) {
            action = str(body, "transferType");
        }
        List<Long> customerIds = ids(body.get("customerIds"));
        if (action == null || action.isBlank()) {
            return Result.error("请选择划拨类型");
        }
        if (customerIds.isEmpty()) {
            return Result.error("请先在列表中勾选客户");
        }
        // 兼容前端 targetId（划拨到组用）与 targetGroupId、ownerId 与 targetOwnerId：
        // 统一回写到 body Map，下方 switch 内已有的 longVal(body.get("targetGroupId")) 等会读到。
        if (longVal(body.get("targetGroupId")) == null && longVal(body.get("targetId")) != null) {
            body.put("targetGroupId", longVal(body.get("targetId")));
        }
        if (longVal(body.get("targetOwnerId")) == null && longVal(body.get("ownerId")) != null) {
            body.put("targetOwnerId", longVal(body.get("ownerId")));
        }
        switch (action.toUpperCase()) {
            case "GROUP" -> {
                Long targetGroupId = longVal(body.get("targetGroupId"));
                if (targetGroupId == null) {
                    return Result.error("划拨到组需要 targetGroupId");
                }
                for (Long cid : customerIds) {
                    OrgGroupCustomer gc = new OrgGroupCustomer();
                    gc.setGroupId(targetGroupId);
                    gc.setCustomerId(cid);
                    groupCustomerMapper.insert(gc);
                }
            }
            case "COMPANY" -> {
                // 回公司池：移除组内客户记录
                for (Long cid : customerIds) {
                    groupCustomerMapper.delete(new LambdaQueryWrapper<OrgGroupCustomer>()
                            .eq(OrgGroupCustomer::getCustomerId, cid));
                }
            }
            case "OWNER" -> {
                Long targetOwnerId = longVal(body.get("targetOwnerId"));
                if (targetOwnerId == null) {
                    return Result.error("批量迁移主要负责人需要 targetOwnerId");
                }
                for (Long cid : customerIds) {
                    OrgMyCustomer mc = myCustomerMapper.selectOne(new LambdaQueryWrapper<OrgMyCustomer>()
                            .eq(OrgMyCustomer::getCustomerId, cid).last("LIMIT 1"));
                    if (mc != null) {
                        mc.setOwnerId(targetOwnerId);
                        myCustomerMapper.updateById(mc);
                    } else {
                        OrgMyCustomer n = new OrgMyCustomer();
                        n.setOwnerId(targetOwnerId);
                        n.setCustomerId(cid);
                        myCustomerMapper.insert(n);
                    }
                }
            }
            default -> {
                return Result.error("不支持的划拨类型：" + action);
            }
        }
        return Result.ok();
    }

    /** 组列表（带 组内客户数 / 我的客户数）。 */
    public Map<String, Object> groupList() {
        List<OrgGroup> groups = groupMapper.selectList(
                new LambdaQueryWrapper<OrgGroup>().orderByAsc(OrgGroup::getId));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (OrgGroup g : groups) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", g.getId());
            row.put("groupName", g.getGroupName());
            row.put("ownerId", g.getOwnerId());
            row.put("ownerName", g.getOwnerName());
            row.put("createTime", g.getCreateTime() != null
                    ? g.getCreateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    : null);
            row.put("customerCnt", groupCustomerMapper.selectCount(new LambdaQueryWrapper<OrgGroupCustomer>()
                    .eq(OrgGroupCustomer::getGroupId, g.getId())));
            rows.add(row);
        }
        return Map.of("list", rows, "total", (long) rows.size());
    }

    private static String str(Map<String, Object> body, String key) {
        Object v = body == null ? null : body.get(key);
        return v == null ? null : String.valueOf(v);
    }

    private static Long longVal(Object v) {
        if (v instanceof Number n) {
            return n.longValue();
        }
        if (v instanceof String s) {
            try {
                return Long.parseLong(s.trim());
            } catch (NumberFormatException ignore) {
                return null;
            }
        }
        return null;
    }

    private static List<Long> ids(Object raw) {
        List<Long> out = new ArrayList<>();
        if (raw instanceof List<?> list) {
            for (Object o : list) {
                Long id = longVal(o);
                if (id != null) {
                    out.add(id);
                }
            }
        }
        return out;
    }
}
