package com.cloud.cms.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.cloud.common.core.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 推荐权重配置对象 cmd_recommend_weight
 */
@Data
@TableName("cms_recommend_weight")
public class CmdRecommendWeight implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 配置ID */
    private String id;

    /** 浏览权重 */
    @Excel(name = "浏览权重")
    private BigDecimal browseWeight;

    /** 收藏权重 */
    @Excel(name = "收藏权重")
    private BigDecimal favoriteWeight;

    /** 1星评价权重 */
    @Excel(name = "1星评价权重")
    @TableField("review_star_1_weight")
    private BigDecimal reviewStar1Weight;

    /** 2星评价权重 */
    @Excel(name = "2星评价权重")
    @TableField("review_star_2_weight")
    private BigDecimal reviewStar2Weight;

    /** 3星评价权重 */
    @Excel(name = "3星评价权重")
    @TableField("review_star_3_weight")
    private BigDecimal reviewStar3Weight;

    /** 4星评价权重 */
    @Excel(name = "4星评价权重")
    @TableField("review_star_4_weight")
    private BigDecimal reviewStar4Weight;

    /** 5星评价权重 */
    @Excel(name = "5星评价权重")
    @TableField("review_star_5_weight")
    private BigDecimal reviewStar5Weight;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "更新时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date updateTime;
}