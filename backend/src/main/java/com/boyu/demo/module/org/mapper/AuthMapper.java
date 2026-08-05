package com.boyu.demo.module.org.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 认证/权限查询：登录时加载角色、菜单、权限码。
 * <p>菜单权限基础表：sys_person / sys_role / sys_menu / sys_person_role / sys_role_menu。
 */
@Mapper
public interface AuthMapper {

    /** 当前人员的全部权限码（BUTTON 类型菜单的 perms）。 */
    @Select("SELECT DISTINCT m.perms FROM sys_menu m " +
            "JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "JOIN sys_person_role pr ON rm.role_id = pr.role_id " +
            "WHERE pr.person_id = #{personId} AND m.type = 'BUTTON' AND m.perms IS NOT NULL")
    List<String> selectPermissionCodes(@Param("personId") Long personId);

    /** 当前人员的菜单路径（MENU 类型，排序稳定）。 */
    @Select("SELECT m.path FROM sys_menu m " +
            "JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "JOIN sys_person_role pr ON rm.role_id = pr.role_id " +
            "WHERE pr.person_id = #{personId} AND m.type = 'MENU' AND m.path IS NOT NULL " +
            "GROUP BY m.path ORDER BY MAX(m.sort)")
    List<String> selectMenuPaths(@Param("personId") Long personId);

    /** 当前人员的角色 id 列表。 */
    @Select("SELECT r.id FROM sys_role r " +
            "JOIN sys_person_role pr ON r.id = pr.role_id " +
            "WHERE pr.person_id = #{personId}")
    List<Long> selectRoleIds(@Param("personId") Long personId);

    /** 为人员绑定一个角色（幂等）。 */
    @Insert("INSERT IGNORE INTO sys_person_role (person_id, role_id) VALUES (#{personId}, #{roleId})")
    int insertPersonRole(@Param("personId") Long personId, @Param("roleId") Long roleId);

    /** 清空人员的全部角色（重建用）。 */
    @Delete("DELETE FROM sys_person_role WHERE person_id = #{personId}")
    int deletePersonRoles(@Param("personId") Long personId);
}
