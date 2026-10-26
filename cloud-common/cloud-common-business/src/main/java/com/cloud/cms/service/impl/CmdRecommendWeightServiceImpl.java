package com.cloud.cms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.cms.domain.CmdRecommendWeight;
import com.cloud.cms.mapper.CmdRecommendWeightMapper;
import com.cloud.cms.service.ICmdRecommendWeightService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Recommendation Weight Configuration Service Implementation
 */
@Service
public class CmdRecommendWeightServiceImpl extends ServiceImpl<CmdRecommendWeightMapper, CmdRecommendWeight> implements ICmdRecommendWeightService {

    @Override
    public CmdRecommendWeight getOrInitWeight() {
        CmdRecommendWeight weight = getById("1");
        if (weight != null) {
            return weight;
        }

        weight = new CmdRecommendWeight();
        weight.setId("1");
        weight.setBrowseWeight(new BigDecimal("1"));
        weight.setFavoriteWeight(new BigDecimal("3"));
        weight.setReviewStar1Weight(new BigDecimal("1"));
        weight.setReviewStar2Weight(new BigDecimal("2"));
        weight.setReviewStar3Weight(new BigDecimal("3"));
        weight.setReviewStar4Weight(new BigDecimal("4"));
        weight.setReviewStar5Weight(new BigDecimal("5"));
        weight.setCreateTime(new Date());
        weight.setUpdateTime(new Date());
        save(weight);
        return weight;
    }
}