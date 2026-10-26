package com.cloud.ums.service.impl;

import java.util.Date;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.cloud.ums.mapper.UmsBrowseHistoryMapper;
import com.cloud.ums.domain.UmsBrowseHistory;
import com.cloud.ums.service.IUmsBrowseHistoryService;

/**
 * Browse History Service Implementation
 *
 * @author cloud
 * @date 2025-03-15
 */
@Service
public class UmsBrowseHistoryServiceImpl extends ServiceImpl<UmsBrowseHistoryMapper, UmsBrowseHistory> implements IUmsBrowseHistoryService {

    @Override
    @Async
    public synchronized void asyncRecordBrowseHistory(String userId, String productId) {
        LambdaQueryWrapper<UmsBrowseHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UmsBrowseHistory::getUserId, userId)
                .eq(UmsBrowseHistory::getProductId, productId);
        UmsBrowseHistory umsBrowseHistory = this.getOne(queryWrapper);
        if (umsBrowseHistory!=null) {
            umsBrowseHistory.setBrowseTime(new Date());
            this.updateById(umsBrowseHistory);
        }else{
            UmsBrowseHistory browseHistory = new UmsBrowseHistory();
            browseHistory.setId(IdUtil.getSnowflakeNextIdStr());
            browseHistory.setUserId(userId);
            browseHistory.setProductId(productId);
            browseHistory.setBrowseTime(new Date());
            this.save(browseHistory);
        }

    }

}
