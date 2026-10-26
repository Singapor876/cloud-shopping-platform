package com.cloud.cms.domain;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.cloud.common.core.annotation.Excel;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 商品促销关联对象 cms_product_promotion
 * 
 * @author cloud
 * @date 2025-03-15
 */
@Data
public class CmsProductPromotion implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 关联ID */
    private String id;
    /** 商品ID */
    @Excel(name = "商品ID")
    private String productId;
    /** 促销ID */
    @Excel(name = "促销ID")
    private String promotionId;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date createTime;
    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "更新时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date updateTime;
}
