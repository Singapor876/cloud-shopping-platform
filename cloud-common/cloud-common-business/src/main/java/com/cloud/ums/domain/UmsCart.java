package com.cloud.ums.domain;


import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.cloud.cms.domain.CmsProduct;
import com.cloud.common.core.annotation.Excel;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 购物车对象 ums_cart
 * 
 * @author cloud
 * @date 2025-03-15
 */
@Data
public class UmsCart implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 购物车ID */
    private String id;
    /** 用户ID */
    @Excel(name = "用户ID")
    private String userId;
    /** 商品ID */
    @Excel(name = "商品ID")
    private String productId;
    /** 商品数量 */
    @Excel(name = "商品数量")
    private Long quantity;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date createTime;
    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "更新时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date updateTime;

    /** 用户数据-用于接口返回 */
    @TableField(exist = false)
    private UmsUser umsUser;
    /** 商品集合-用于接口返回 */
    @TableField(exist = false)
    private List<CmsProduct> cmsProductList;
    /** 商品ID集合-用于数据库分组接参 */
    @TableField(exist = false)
    private String productIds;
    /** 商品数量-用于数据库分组接参 */
    @TableField(exist = false)
    private String quantityList;
    /** 用户数据-用于前端接收 */
    @TableField(exist = false)
    private String userName;
}
