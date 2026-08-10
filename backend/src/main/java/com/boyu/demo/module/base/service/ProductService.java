package com.boyu.demo.module.base.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyu.demo.module.base.entity.Product;
import com.boyu.demo.module.base.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 产品主数据服务（品名→牌号→材质元素 六项维护 + 作废级联）。
 */
@Service
public class ProductService extends ServiceImpl<ProductMapper, Product> {

    /**
     * 按账套构建产品树：品名根节点（parent_id=0）→ 牌号 → 材质元素。
     */
    public List<Map<String, Object>> buildTree(Long accountId) {
        List<Product> all = lambdaQuery()
                .eq(accountId != null, Product::getAccountId, accountId)
                .eq(Product::getStatus, 1)
                .orderByAsc(Product::getSort)
                .list();
        Map<Long, List<Product>> byParent = all.stream()
                .collect(Collectors.groupingBy(p -> p.getParentId() == null ? 0L : p.getParentId()));
        List<Map<String, Object>> roots = new ArrayList<>();
        for (Product p : all) {
            if (p.getParentId() == null || p.getParentId() == 0) {
                roots.add(toNode(p, byParent, "product"));
            }
        }
        return roots;
    }

    /**
     * 作废产品（级联：下级数据都会被作废）。
     */
    @Transactional
    public void invalidate(Long id) {
        List<Long> ids = new ArrayList<>();
        collectIds(id, ids);
        LambdaUpdateWrapper<Product> uw = new LambdaUpdateWrapper<>();
        uw.in(Product::getId, ids).set(Product::getStatus, 0);
        update(uw);
    }

    /**
     * 构建树节点（含 type）：品名根节点 → product；牌号 → grade；材质元素 → material。
     *
     * @param type 当前节点类型（由父类型推断，前端 ProductNode.type 对齐）
     */
    private Map<String, Object> toNode(Product p, Map<Long, List<Product>> byParent, String type) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("id", p.getId());
        node.put("accountId", p.getAccountId());
        node.put("name", p.getName());
        node.put("type", type);
        node.put("grade", p.getGrade());
        node.put("material", p.getMaterial());
        node.put("spec", p.getSpec());
        node.put("brandOrigin", p.getBrandOrigin());
        node.put("other", p.getOther());
        node.put("sort", p.getSort());
        node.put("status", p.getStatus());
        List<Map<String, Object>> children = new ArrayList<>();
        String childType = "product".equals(type) ? "grade" : "material";
        for (Product sub : byParent.getOrDefault(p.getId(), List.of())) {
            children.add(toNode(sub, byParent, childType));
        }
        node.put("children", children);
        return node;
    }

    private void collectIds(Long id, List<Long> out) {
        out.add(id);
        List<Product> children = lambdaQuery().eq(Product::getParentId, id).list();
        for (Product c : children) {
            collectIds(c.getId(), out);
        }
    }
}
