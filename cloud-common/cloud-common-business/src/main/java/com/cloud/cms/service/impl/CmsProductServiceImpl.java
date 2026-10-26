package com.cloud.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.cms.domain.CmsProduct;
import com.cloud.cms.domain.CmsProductPromotion;
import com.cloud.cms.domain.CmsPromotion;
import com.cloud.cms.mapper.CmsProductMapper;
import com.cloud.cms.service.ICmsProductPromotionService;
import com.cloud.cms.service.ICmsProductService;
import com.cloud.cms.service.ICmsPromotionService;
import com.cloud.oms.enums.PromotionType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Product Service Implementation
 * 
 * @author cloud
 * @date 2025-03-15
 */
@Service
public class CmsProductServiceImpl extends ServiceImpl<CmsProductMapper, CmsProduct> implements ICmsProductService {
    @Resource
    private ICmsProductPromotionService productPromotionService;
    @Resource
    private ICmsPromotionService promotionService;

    public void queryIsPromotion(CmsProduct cmsProduct) {
        LambdaQueryWrapper<CmsProductPromotion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CmsProductPromotion::getProductId, cmsProduct.getId());
        CmsProductPromotion productPromotion = productPromotionService.getOne(queryWrapper);
        if (productPromotion!=null){
            String promotionId = productPromotion.getPromotionId();
            CmsPromotion cmsPromotion = promotionService.getById(promotionId);
            Long type = cmsPromotion.getType();
            if (Objects.equals(type, PromotionType.ZK.getValue())){
                BigDecimal discount = cmsPromotion.getDiscount();
                cmsProduct.setIsDiscount(true);
                cmsProduct.setDiscountNum(discount);
            }
            if (Objects.equals(type, PromotionType.YH.getValue())){
                BigDecimal couponValue = cmsPromotion.getCouponValue();
                cmsProduct.setIsDiscount(false);
                cmsProduct.setDiscountNum(couponValue);
            }
        }
    }
}
