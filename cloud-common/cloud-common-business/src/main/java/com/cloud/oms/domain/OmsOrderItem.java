package com.cloud.oms.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.cloud.cms.domain.CmsProduct;
import com.cloud.common.core.annotation.Excel;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单商品对象 oms_order_item
 * 
 * @author cloud
 * @date 2025-03-15
 */
@Data
public class OmsOrderItem implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 订单商品ID */
    private String id;
    /** 订单ID */
    @Excel(name = "订单ID")
    private String orderId;
    /** 商品ID */
    @Excel(name = "商品ID")
    private String productId;
    /** 商品数量 */
    @Excel(name = "商品数量")
    private Long quantity;
    /** 商品单价 */
    @Excel(name = "商品单价")
    private BigDecimal price;
    /** 是否已评论 */
    @Excel(name = "是否已评论")
    private Boolean isReview;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date createTime;
    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "更新时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date updateTime;
    /** 商品详情 */
    @TableField(exist = false)
    private CmsProduct cmsProduct;

}
