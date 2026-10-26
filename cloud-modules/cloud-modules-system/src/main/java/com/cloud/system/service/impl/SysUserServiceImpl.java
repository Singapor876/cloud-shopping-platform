package com.cloud.system.service.impl;

import com.cloud.common.core.constant.UserConstants;
import com.cloud.common.core.exception.ServiceException;
import com.cloud.common.core.utils.SpringUtils;
import com.cloud.common.core.utils.StringUtils;
import com.cloud.common.core.utils.bean.BeanValidators;
import com.cloud.common.datascope.annotation.DataScope;
import com.cloud.common.security.utils.SecurityUtils;
import com.cloud.system.api.domain.SysRole;
import com.cloud.system.api.domain.SysUser;
import com.cloud.system.api.model.LoginUser;
import com.cloud.system.domain.SysPost;
import com.cloud.system.domain.SysUserPost;
import com.cloud.system.domain.SysUserRole;
import com.cloud.system.mapper.*;
import com.cloud.system.service.ISysConfigService;
import com.cloud.system.service.ISysUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User Business Layer Processing
 * 
 * @author cloud
 */
@Service
public class SysUserServiceImpl implements ISysUserService
{
    private static final Logger log = LoggerFactory.getLogger(SysUserServiceImpl.class);

    @Resource
    private SysUserMapper userMapper;

    @Resource
    private SysRoleMapper roleMapper;

    @Resource
    private SysPostMapper postMapper;

    @Resource
    private SysUserRoleMapper userRoleMapper;

    @Resource
    private SysUserPostMapper userPostMapper;

    @Resource
    private ISysConfigService configService;

    @Resource
    protected Validator validator;

    /**
     * Query user list by condition with pagination
     * 
     * @param user User information
     * @return User information collection
     */
    @Override
    @DataScope(deptAlias = "d", userAlias = "u")
    public List<SysUser> selectUserList(SysUser user)
    {
        return userMapper.selectUserList(user);
    }

    /**
     * Query assigned user role list by condition with pagination
     * 
     * @param user User information
     * @return User information collection
     */
    @Override
    @DataScope(deptAlias = "d", userAlias = "u")
    public List<SysUser> selectAllocatedList(SysUser user)
    {
        return userMapper.selectAllocatedList(user);
    }

    /**
     * Query unassigned user role list by condition with pagination
     * 
     * @param user User information
     * @return User information collection
     */
    @Override
    @DataScope(deptAlias = "d", userAlias = "u")
    public List<SysUser> selectUnallocatedList(SysUser user)
    {
        return userMapper.selectUnallocatedList(user);
    }

    /**
     * Query user by username
     * 
     * @param userName Username
     * @return User object information
     */
    @Override
    public SysUser selectUserByUserName(String userName)
    {
        return userMapper.selectUserByUserName(userName);
    }

    /**
     * Query user by user ID
     * 
     * @param userId User ID
     * @return User object information
     */
    @Override
    public SysUser selectUserById(Long userId)
    {
        return userMapper.selectUserById(userId);
    }

    /**
     * Query user's role group
     * 
     * @param userName Username
     * @return Result
     */
    @Override
    public String selectUserRoleGroup(String userName)
    {
        List<SysRole> list = roleMapper.selectRolesByUserName(userName);
        if (CollectionUtils.isEmpty(list))
        {
            return StringUtils.EMPTY;
        }
        return list.stream().map(SysRole::getRoleName).collect(Collectors.joining(","));
    }

    /**
     * Query user's post group
     * 
     * @param userName Username
     * @return Result
     */
    @Override
    public String selectUserPostGroup(String userName)
    {
        List<SysPost> list = postMapper.selectPostsByUserName(userName);
        if (CollectionUtils.isEmpty(list))
        {
            return StringUtils.EMPTY;
        }
        return list.stream().map(SysPost::getPostName).collect(Collectors.joining(","));
    }

    /**
     * Check if username is unique
     * 
     * @param userName Username
     * @return Result
     */
    @Override
    public String checkUserNameUnique(String userName)
    {
        int count = userMapper.checkUserNameUnique(userName);
        if (count > 0)
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * Check if phone number is unique
     *
     * @param user User information
     * @return
     */
    @Override
    public String checkPhoneUnique(SysUser user)
    {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = userMapper.checkPhoneUnique(user.getPhonenumber());
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * Check if email is unique
     *
     * @param user User information
     * @return
     */
    @Override
    public String checkEmailUnique(SysUser user)
    {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = userMapper.checkEmailUnique(user.getEmail());
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * Check if user is allowed to operate
     * 
     * @param user User information
     */
    @Override
    public void checkUserAllowed(SysUser user)
    {
        if (StringUtils.isNotNull(user.getUserId()) && user.isAdmin())
        {
            throw new ServiceException("Not allowed to operate super administrator user");
        }
    }

    /**
     * Check if user has data permission
     * 
     * @param userId User ID
     */
    @Override
    public void checkUserDataScope(Long userId)
    {
        if (!SysUser.isAdmin(SecurityUtils.getUserId()))
        {
            SysUser user = new SysUser();
            user.setUserId(userId);
            List<SysUser> users = SpringUtils.getAopProxy(this).selectUserList(user);
            if (StringUtils.isEmpty(users))
            {
                throw new ServiceException("No permission to access user data!");
            }
        }
    }

    /**
     * Add and save user information
     * 
     * @param user User information
     * @return Result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertUser(SysUser user)
    {
        // Add user information
        int rows = userMapper.insertUser(user);
        // Add user post association
        insertUserPost(user);
        // Add user and role management
        insertUserRole(user);
        return rows;
    }

    /**
     * Register user information
     * 
     * @param user User information
     * @return Result
     */
    @Override
    public boolean registerUser(SysUser user)
    {
        return userMapper.insertUser(user) > 0;
    }

    @Override
    public Boolean payPlaceOrderAmount(BigDecimal totalAmount) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long userid = loginUser.getUserid();
        SysUser sysUser = userMapper.selectUserById(userid);
        BigDecimal balance = sysUser.getBalance();
        if (balance.compareTo(totalAmount)>=0){
            userMapper.payPlaceOrderAmount(userid, totalAmount);
            return true;
        }
        return false;
    }

    /**
     * Update and save user information
     * 
     * @param user User information
     * @return Result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateUser(SysUser user)
    {
        Long userId = user.getUserId();
        // Delete user and role association
        userRoleMapper.deleteUserRoleByUserId(userId);
        // Add user and role management
        insertUserRole(user);
        // Delete user and post association
        userPostMapper.deleteUserPostByUserId(userId);
        // Add user and post management
        insertUserPost(user);
        return userMapper.updateUser(user);
    }

    /**
     * User authorize role
     * 
     * @param userId User ID
     * @param roleIds Role group
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertUserAuth(Long userId, Long[] roleIds)
    {
        userRoleMapper.deleteUserRoleByUserId(userId);
        insertUserRole(userId, roleIds);
    }

    /**
     * Update user status
     * 
     * @param user User information
     * @return Result
     */
    @Override
    public int updateUserStatus(SysUser user)
    {
        return userMapper.updateUser(user);
    }

    /**
     * Update user basic information
     * 
     * @param user User information
     * @return Result
     */
    @Override
    public int updateUserProfile(SysUser user)
    {
        return userMapper.updateUser(user);
    }

    /**
     * Update user avatar
     * 
     * @param userName Username
     * @param avatar Avatar address
     * @return Result
     */
    @Override
    public boolean updateUserAvatar(String userName, String avatar)
    {
        return userMapper.updateUserAvatar(userName, avatar) > 0;
    }

    /**
     * Reset user password
     * 
     * @param user User information
     * @return Result
     */
    @Override
    public int resetPwd(SysUser user)
    {
        return userMapper.updateUser(user);
    }

    /**
     * Reset user password
     * 
     * @param userName Username
     * @param password Password
     * @return Result
     */
    @Override
    public int resetUserPwd(String userName, String password)
    {
        return userMapper.resetUserPwd(userName, password);
    }

    /**
     * Add user role information
     * 
     * @param user User object
     */
    public void insertUserRole(SysUser user)
    {
        Long[] roles = user.getRoleIds();
        if (StringUtils.isNotNull(roles))
        {
            // 新增用户与角色管理
            List<SysUserRole> list = new ArrayList<SysUserRole>();
            for (Long roleId : roles)
            {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(user.getUserId());
                ur.setRoleId(roleId);
                list.add(ur);
            }
            if (list.size() > 0)
            {
                userRoleMapper.batchUserRole(list);
            }
        }
    }

    /**
     * Add user post information
     * 
     * @param user User object
     */
    public void insertUserPost(SysUser user)
    {
        Long[] posts = user.getPostIds();
        if (StringUtils.isNotNull(posts))
        {
            // Add user and post management
            List<SysUserPost> list = new ArrayList<SysUserPost>();
            for (Long postId : posts)
            {
                SysUserPost up = new SysUserPost();
                up.setUserId(user.getUserId());
                up.setPostId(postId);
                list.add(up);
            }
            if (list.size() > 0)
            {
                userPostMapper.batchUserPost(list);
            }
        }
    }

    /**
     * Add user role information
     * 
     * @param userId User ID
     * @param roleIds Role group
     */
    public void insertUserRole(Long userId, Long[] roleIds)
    {
        if (StringUtils.isNotNull(roleIds))
        {
            // 新增用户与角色管理
            List<SysUserRole> list = new ArrayList<SysUserRole>();
            for (Long roleId : roleIds)
            {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                list.add(ur);
            }
            if (list.size() > 0)
            {
                userRoleMapper.batchUserRole(list);
            }
        }
    }

    /**
     * Delete user by user ID
     * 
     * @param userId User ID
     * @return Result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteUserById(Long userId)
    {
        // Delete user and role association
        userRoleMapper.deleteUserRoleByUserId(userId);
        // Delete user and post table
        userPostMapper.deleteUserPostByUserId(userId);
        return userMapper.deleteUserById(userId);
    }

    /**
     * Batch delete user information
     * 
     * @param userIds User IDs to delete
     * @return Result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteUserByIds(Long[] userIds)
    {
        for (Long userId : userIds)
        {
            checkUserAllowed(new SysUser(userId));
            checkUserDataScope(userId);
        }
        // Delete user and role association
        userRoleMapper.deleteUserRole(userIds);
        // Delete user and post association
        userPostMapper.deleteUserPost(userIds);
        return userMapper.deleteUserByIds(userIds);
    }

    /**
     * Import user data
     * 
     * @param userList User data list
     * @param isUpdateSupport Whether to support update, if exists, update data
     * @param operName Operator
     * @return Result
     */
    @Override
    public String importUser(List<SysUser> userList, Boolean isUpdateSupport, String operName)
    {
        if (StringUtils.isNull(userList) || userList.size() == 0)
        {
            throw new ServiceException("Import user data cannot be empty!");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        String password = configService.selectConfigByKey("sys.user.initPassword");
        for (SysUser user : userList)
        {
            try
            {
                // Verify if this user exists
                SysUser u = userMapper.selectUserByUserName(user.getUserName());
                if (StringUtils.isNull(u))
                {
                    BeanValidators.validateWithException(validator, user);
                    user.setPassword(SecurityUtils.encryptPassword(password));
                    user.setCreateBy(operName);
                    this.insertUser(user);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、Account " + user.getUserName() + " imported successfully");
                }
                else if (isUpdateSupport)
                {
                    BeanValidators.validateWithException(validator, user);
                    user.setUpdateBy(operName);
                    this.updateUser(user);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、Account " + user.getUserName() + " updated successfully");
                }
                else
                {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、Account " + user.getUserName() + " already exists");
                }
            }
            catch (Exception e)
            {
                failureNum++;
                String msg = "<br/>" + failureNum + "、Account " + user.getUserName() + " import failed: ";
                failureMsg.append(msg + e.getMessage());
                log.error(msg, e);
            }
        }
        if (failureNum > 0)
        {
            failureMsg.insert(0, "Sorry, import failed! Total " + failureNum + " data format incorrect, errors as follows:");
            throw new ServiceException(failureMsg.toString());
        }
        else
        {
            successMsg.insert(0, "Congratulations, all data imported successfully! Total " + successNum + " items, data as follows:");
        }
        return successMsg.toString();
    }

    @Override
    public BigDecimal getBalance() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long userid = loginUser.getUserid();
        SysUser sysUser = userMapper.selectUserById(userid);
        return sysUser.getBalance();
    }

    @Override
    public int resetBalance() {
        SysUser sysUser = new SysUser();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long userid = loginUser.getUserid();
        sysUser.setUserId(userid);
        sysUser.setBalance(new BigDecimal("10000"));
        userMapper.updateUser(sysUser);
        return 10000;
    }

}
