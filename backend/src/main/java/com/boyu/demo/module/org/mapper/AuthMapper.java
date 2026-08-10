package com.boyu.demo.module.org.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 认证/权限查询：登录时加载角色、菜单、权限码。
 * <p>菜单权限基础表：sys_person / sys_role / sys_menu / sys_person_role / sys_role_menu。
 * <p>SQL 语句定义在 {@code resources/mapper/AuthMapper.xml}（XML 映射方式）。
 */
@Mapper
public interface AuthMapper {

    /** 当前人员的全部权限码（BUTTON 类型菜单的 perms）。 */
    List<String> selectPermissionCodes(@Param("personId") Long personId);

    /** 当前人员的菜单路径（MENU 类型，排序稳定）。 */
    List<String> selectMenuPaths(@Param("personId") Long personId);

    /** 当前人员的角色 id 列表。 */
    List<Long> selectRoleIds(@Param("personId") Long personId);

    /** 当前人员的角色名列表（权限来源：系统管理/组长/组员）。 */
    List<String> selectRoleNames(@Param("personId") Long personId);

    /** 当前人员的权限明细（BUTTON 菜单：权限码 + 中文名 + 所属模块前缀）。 */
    List<Map<String, Object>> selectPermissionDetails(@Param("personId") Long personId);

    /** 为人员绑定一个角色（幂等）。 */
    int insertPersonRole(@Param("personId") Long personId, @Param("roleId") Long roleId);

    /** 清空人员的全部角色（重建用）。 */
    int deletePersonRoles(@Param("personId") Long personId);
}
