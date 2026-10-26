package com.cloud.ums.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.cloud.common.core.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户对象 ums_user
 *
 * @author haiziohhue
 * @date 2024-11-11
 */
@Data
public class UmsUser implements Serializable{
    private static final long serialVersionUID = 1L;

    /** 用户id */
    @TableId
    private String userId;
    /** 用户名 */
    @Excel(name = "用户名")
    private String userName;
    /** 头像 */
    @Excel(name = "头像")
    private String userAvatar;
    /** 密码 */
    @Excel(name = "密码")
    private String userPassword;
    /** 电话 */
    @Excel(name = "电话")
    private String userPhone;
    /** 用户虚拟余额 */
    @Excel(name = "用户虚拟余额")
    private BigDecimal userBalance;
    /** 用户地址json格式 */
    @Excel(name = "用户地址json格式")
    private String userAddress;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date createTime;
    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "更新时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date updateTime;
}
