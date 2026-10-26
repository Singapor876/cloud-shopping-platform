package com.cloud.system.service.impl;

import com.cloud.common.core.constant.UserConstants;
import com.cloud.common.core.exception.ServiceException;
import com.cloud.common.core.utils.SpringUtils;
import com.cloud.common.core.utils.StringUtils;
import com.cloud.common.datascope.annotation.DataScope;
import com.cloud.common.security.utils.SecurityUtils;
import com.cloud.system.api.domain.SysRole;
import com.cloud.system.api.domain.SysUser;
import com.cloud.system.domain.SysRoleDept;
import com.cloud.system.domain.SysRoleMenu;
import com.cloud.system.domain.SysUserRole;
import com.cloud.system.mapper.SysRoleDeptMapper;
import com.cloud.system.mapper.SysRoleMapper;
import com.cloud.system.mapper.SysRoleMenuMapper;
import com.cloud.system.mapper.SysUserRoleMapper;
import com.cloud.system.service.ISysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Role Business Layer Processing
 * 
 * @author cloud
 */
@Service
public class SysRoleServiceImpl implements ISysRoleService
{
    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysRoleMenuMapper roleMenuMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SysRoleDeptMapper roleDeptMapper;

    /**
     * Query role data by condition with pagination
     * 
     * @param role Role information
     * @return Role data collection information
     */
    @Override
    @DataScope(deptAlias = "d")
    public List<SysRole> selectRoleList(SysRole role)
    {
        return roleMapper.selectRoleList(role);
    }

    /**
     * Query roles by user ID
     * 
     * @param userId User ID
     * @return Role list
     */
    @Override
    public List<SysRole> selectRolesByUserId(Long userId)
    {
        List<SysRole> userRoles = roleMapper.selectRolePermissionByUserId(userId);
        List<SysRole> roles = selectRoleAll();
        for (SysRole role : roles)
        {
            for (SysRole userRole : userRoles)
            {
                if (role.getRoleId().longValue() == userRole.getRoleId().longValue())
                {
                    role.setFlag(true);
                    break;
                }
            }
        }
        return roles;
    }

    /**
     * Query permissions by user ID
     * 
     * @param userId User ID
     * @return Permission list
     */
    @Override
    public Set<String> selectRolePermissionByUserId(Long userId)
    {
        List<SysRole> perms = roleMapper.selectRolePermissionByUserId(userId);
        Set<String> permsSet = new HashSet<>();
        for (SysRole perm : perms)
        {
            if (StringUtils.isNotNull(perm))
            {
                permsSet.addAll(Arrays.asList(perm.getRoleKey().trim().split(",")));
            }
        }
        return permsSet;
    }

    /**
     * Query all roles
     * 
     * @return Role list
     */
    @Override
    public List<SysRole> selectRoleAll()
    {
        return SpringUtils.getAopProxy(this).selectRoleList(new SysRole());
    }

    /**
     * Get role selection list by user ID
     * 
     * @param userId User ID
     * @return Selected role ID list
     */
    @Override
    public List<Long> selectRoleListByUserId(Long userId)
    {
        return roleMapper.selectRoleListByUserId(userId);
    }

    /**
     * Query role by role ID
     * 
     * @param roleId Role ID
     * @return Role object information
     */
    @Override
    public SysRole selectRoleById(Long roleId)
    {
        return roleMapper.selectRoleById(roleId);
    }

    /**
     * Check if role name is unique
     * 
     * @param role Role information
     * @return Result
     */
    @Override
    public String checkRoleNameUnique(SysRole role)
    {
        Long roleId = StringUtils.isNull(role.getRoleId()) ? -1L : role.getRoleId();
        SysRole info = roleMapper.checkRoleNameUnique(role.getRoleName());
        if (StringUtils.isNotNull(info) && info.getRoleId().longValue() != roleId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * Check if role permission is unique
     * 
     * @param role Role information
     * @return Result
     */
    @Override
    public String checkRoleKeyUnique(SysRole role)
    {
        Long roleId = StringUtils.isNull(role.getRoleId()) ? -1L : role.getRoleId();
        SysRole info = roleMapper.checkRoleKeyUnique(role.getRoleKey());
        if (StringUtils.isNotNull(info) && info.getRoleId().longValue() != roleId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * Check if role is allowed to be operated
     * 
     * @param role Role information
     */
    @Override
    public void checkRoleAllowed(SysRole role)
    {
        if (StringUtils.isNotNull(role.getRoleId()) && role.isAdmin())
        {
            throw new ServiceException("Not allowed to operate super administrator role");
        }
    }

    /**
     * Check if role has data permission
     * 
     * @param roleId Role ID
     */
    @Override
    public void checkRoleDataScope(Long roleId)
    {
        if (!SysUser.isAdmin(SecurityUtils.getUserId()))
        {
            SysRole role = new SysRole();
            role.setRoleId(roleId);
            List<SysRole> roles = SpringUtils.getAopProxy(this).selectRoleList(role);
            if (StringUtils.isEmpty(roles))
            {
                throw new ServiceException("No permission to access role data!");
            }
        }
    }

    /**
     * Query role usage count by role ID
     * 
     * @param roleId Role ID
     * @return Result
     */
    @Override
    public int countUserRoleByRoleId(Long roleId)
    {
        return userRoleMapper.countUserRoleByRoleId(roleId);
    }

    /**
     * Add and save role information
     * 
     * @param role Role information
     * @return Result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertRole(SysRole role)
    {
        // Add role information
        roleMapper.insertRole(role);
        return insertRoleMenu(role);
    }

    /**
     * Update and save role information
     * 
     * @param role Role information
     * @return Result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateRole(SysRole role)
    {
        // Update role information
        roleMapper.updateRole(role);
        // Delete role-menu association
        roleMenuMapper.deleteRoleMenuByRoleId(role.getRoleId());
        return insertRoleMenu(role);
    }

    /**
     * Update role status
     * 
     * @param role Role information
     * @return Result
     */
    @Override
    public int updateRoleStatus(SysRole role)
    {
        return roleMapper.updateRole(role);
    }

    /**
     * Update data permission information
     * 
     * @param role Role information
     * @return Result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int authDataScope(SysRole role)
    {
        // Update role information
        roleMapper.updateRole(role);
        // Delete role-department association
        roleDeptMapper.deleteRoleDeptByRoleId(role.getRoleId());
        // Add role and department information (data permission)
        return insertRoleDept(role);
    }

    /**
     * Add role menu information
     * 
     * @param role Role object
     */
    public int insertRoleMenu(SysRole role)
    {
        int rows = 1;
        // Add user-role management
        List<SysRoleMenu> list = new ArrayList<SysRoleMenu>();
        for (Long menuId : role.getMenuIds())
        {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(role.getRoleId());
            rm.setMenuId(menuId);
            list.add(rm);
        }
        if (list.size() > 0)
        {
            rows = roleMenuMapper.batchRoleMenu(list);
        }
        return rows;
    }

    /**
     * Add role department information (data permission)
     *
     * @param role Role object
     */
    public int insertRoleDept(SysRole role)
    {
        int rows = 1;
        // Add role-department (data permission) management
        List<SysRoleDept> list = new ArrayList<SysRoleDept>();
        for (Long deptId : role.getDeptIds())
        {
            SysRoleDept rd = new SysRoleDept();
            rd.setRoleId(role.getRoleId());
            rd.setDeptId(deptId);
            list.add(rd);
        }
        if (list.size() > 0)
        {
            rows = roleDeptMapper.batchRoleDept(list);
        }
        return rows;
    }

    /**
     * Delete role by role ID
     * 
     * @param roleId Role ID
     * @return Result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteRoleById(Long roleId)
    {
        // Delete role-menu association
        roleMenuMapper.deleteRoleMenuByRoleId(roleId);
        // Delete role-department association
        roleDeptMapper.deleteRoleDeptByRoleId(roleId);
        return roleMapper.deleteRoleById(roleId);
    }

    /**
     * Batch delete role information
     * 
     * @param roleIds Role IDs to delete
     * @return Result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteRoleByIds(Long[] roleIds)
    {
        for (Long roleId : roleIds)
        {
            checkRoleAllowed(new SysRole(roleId));
            checkRoleDataScope(roleId);
            SysRole role = selectRoleById(roleId);
            if (countUserRoleByRoleId(roleId) > 0)
            {
                throw new ServiceException(String.format("%1$s is already assigned, cannot be deleted", role.getRoleName()));
            }
        }
        // Delete role-menu association
        roleMenuMapper.deleteRoleMenu(roleIds);
        // Delete role-department association
        roleDeptMapper.deleteRoleDept(roleIds);
        return roleMapper.deleteRoleByIds(roleIds);
    }

    /**
     * Cancel authorized user role
     * 
     * @param userRole User and role association information
     * @return Result
     */
    @Override
    public int deleteAuthUser(SysUserRole userRole)
    {
        return userRoleMapper.deleteUserRoleInfo(userRole);
    }

    /**
     * Batch cancel authorized user roles
     * 
     * @param roleId Role ID
     * @param userIds User data IDs to cancel authorization
     * @return Result
     */
    @Override
    public int deleteAuthUsers(Long roleId, Long[] userIds)
    {
        return userRoleMapper.deleteUserRoleInfos(roleId, userIds);
    }

    /**
     * Batch select authorized user roles
     * 
     * @param roleId Role ID
     * @param userIds User data IDs to authorize
     * @return Result
     */
    @Override
    public int insertAuthUsers(Long roleId, Long[] userIds)
    {
        // Add user-role management
        List<SysUserRole> list = new ArrayList<SysUserRole>();
        for (Long userId : userIds)
        {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            list.add(ur);
        }
        return userRoleMapper.batchUserRole(list);
    }
}
