package com.cloud.cms.service;

import com.cloud.cms.domain.CmsProduct;

import java.util.List;

/**
 * 推荐商品Service接口
 */
public interface ICmdRecommendService {

    List<CmsProduct> recommendProducts(String userId);
}