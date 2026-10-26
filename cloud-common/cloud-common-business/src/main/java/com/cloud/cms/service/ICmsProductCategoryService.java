package com.cloud.cms.service;

import java.util.List;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.cms.domain.CmsProductCategory;

/**
 * 商品分类Service接口
 * 
 * @author cloud
 * @date 2025-03-15
 */
public interface ICmsProductCategoryService extends IService<CmsProductCategory> {

    List<CmsProductCategory> buildTree(List<CmsProductCategory> categories);
    List<CmsProductCategory> getAllLeafNodes(List<CmsProductCategory> categories);
}
