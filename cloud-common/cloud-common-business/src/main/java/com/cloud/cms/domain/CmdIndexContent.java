package com.cloud.cms.domain;


import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.cloud.common.core.annotation.Excel;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 首页内容对象 cmd_index_content
 *
 * @author cloud
 * @date 2025-03-19
 */
@Data
@TableName("cms_index_content")
public class CmdIndexContent implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 内容管理ID */
    private String id;
    /** 轮播图 */
    @Excel(name = "轮播图")
    private String imageUrl;
    /** 内容描述富文本 */
    @Excel(name = "内容描述富文本")
    private String description;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date createTime;
    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "更新时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date updateTime;
}

