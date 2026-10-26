package com.cloud.cms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.cms.domain.CmsProduct;

/**
 * 商品Service接口
 * 
 * @author cloud
 * @date 2025-03-15
 */
public interface ICmsProductService extends IService<CmsProduct> {

    void queryIsPromotion(CmsProduct cmsProduct);
}
