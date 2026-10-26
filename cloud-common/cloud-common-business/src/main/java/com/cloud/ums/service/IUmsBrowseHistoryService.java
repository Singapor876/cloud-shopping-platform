package com.cloud.ums.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.ums.domain.UmsBrowseHistory;

/**
 * 浏览记录Service接口
 * 
 * @author cloud
 * @date 2025-03-15
 */
public interface IUmsBrowseHistoryService extends IService<UmsBrowseHistory> {
    /**
     * 异步记录商品浏览历史
     *
     * @param userId    用户ID
     * @param productId 商品ID
     */
    void asyncRecordBrowseHistory(String userId, String productId);
}
