package com.cloud.cms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.cms.domain.CmdRecommendWeight;

/**
 * 推荐权重配置Service接口
 */
public interface ICmdRecommendWeightService extends IService<CmdRecommendWeight> {

    CmdRecommendWeight getOrInitWeight();
}