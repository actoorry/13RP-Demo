package com.boyu.demo.common;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 分页查询基类：前端传 page / size，转换为 MyBatis-Plus 分页参数。
 * <p>契约 §3.1：分页返回 {@code data: { list: [...], total: n }}。
 */
public class PageQuery {

    /** 页码，从 1 开始。 */
    private long page = 1;
    /** 每页条数。 */
    private long size = 10;

    public long getPage() {
        return Math.max(page, 1);
    }

    public void setPage(long page) {
        this.page = page;
    }

    public long getSize() {
        return size <= 0 ? 10 : size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    /**
     * 将 MyBatis-Plus 分页结果转换为契约要求的 {@code {list, total}} Map。
     */
    public static Map<String, Object> toPageMap(IPage<?> page) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("list", page.getRecords());
        m.put("total", page.getTotal());
        return m;
    }
}
