package com.cloud.cms.controller.front;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloud.cms.domain.CmsProduct;
import com.cloud.cms.enums.ProductStatus;
import com.cloud.cms.service.ICmsProductService;
import com.cloud.common.core.utils.StringUtils;
import com.cloud.common.core.web.controller.BaseController;
import com.cloud.common.security.utils.FrontSecurityUtils;
import com.cloud.ums.domain.AjaxResultResponse;
import com.cloud.ums.domain.UmsFavorite;
import com.cloud.ums.domain.UmsProductReview;
import com.cloud.ums.service.IUmsBrowseHistoryService;
import com.cloud.ums.service.IUmsFavoriteService;
import com.cloud.ums.service.IUmsProductReviewService;
import com.cloud.ums.service.IUmsUserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * Product Controller
 * 
 * @author cloud
 * @date 2025-03-15
 */
@RestController
@RequestMapping("/front/product")
public class CmsProductControllerFront extends BaseController {
    @Resource
    private ICmsProductService cmsProductService;
    @Resource
    private IUmsBrowseHistoryService umsBrowseHistoryService;
    @Resource
    private IUmsProductReviewService umsProductReviewService;
    @Resource
    private IUmsUserService umsUserService;
    @Resource
    private IUmsFavoriteService umsFavoriteService;

    /**
     * Query product list
     */
    @GetMapping("/list")
    public AjaxResultResponse<List<CmsProduct>> list(CmsProduct cmsProduct) {
        LambdaQueryWrapper<CmsProduct> queryWrapper = new LambdaQueryWrapper<>(cmsProduct);
        queryWrapper.orderByDesc(CmsProduct::getUpdateTime)
                .eq(CmsProduct::getProductStatus, ProductStatus.UNSOLD.getValue());
        List<CmsProduct> list = cmsProductService.list(queryWrapper);
        return AjaxResultResponse.success(list);
    }

    /**
     * Search products
     */
    @GetMapping(value = "/searchProduct")
    public AjaxResultResponse<List<CmsProduct>> searchProduct(
            @RequestParam(value = "searchContent", required = false) String searchContent,
            @RequestParam(value = "categoryId", required = false) String categoryId ) {
        LambdaQueryWrapper<CmsProduct> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(CmsProduct::getUpdateTime);
        if (StringUtils.isNotBlank(searchContent)){
            queryWrapper.and(wrapper -> wrapper.like(CmsProduct::getName, searchContent)
                    .or().like(CmsProduct::getDescription, searchContent));
        }
        if (StringUtils.isNotBlank(categoryId)) {
            queryWrapper.eq(CmsProduct::getCategoryId, categoryId);
        }
        List<CmsProduct> cmsProductList = cmsProductService.list(queryWrapper);
        return AjaxResultResponse.success(cmsProductList);
    }

    /**
     * Get product details - login required
     */
    @GetMapping(value = "/getById/{id}")
    public AjaxResultResponse<CmsProduct> getInfo(@PathVariable("id") String productId) {
        if (StringUtils.isEmpty(productId)){
            return AjaxResultResponse.error("ID cannot be empty", null);
        }
        CmsProduct cmsProduct = cmsProductService.getById(productId);
        LambdaQueryWrapper<UmsProductReview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UmsProductReview::getProductId, productId);
        queryWrapper.orderByDesc(UmsProductReview::getUpdateTime);
        List<UmsProductReview> list = umsProductReviewService.list(queryWrapper);
        list.forEach(productReview -> {
            productReview.setUserName(umsUserService.getById(productReview.getUserId()).getUserName());
        });
        cmsProduct.setProductReviewList(list);
        LambdaQueryWrapper<UmsFavorite> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UmsFavorite::getProductId, productId)
                .eq(UmsFavorite::getUserId, FrontSecurityUtils.getUserId());
        UmsFavorite umsFavorite = umsFavoriteService.getOne(lambdaQueryWrapper);
        cmsProduct.setIsCollect(umsFavorite != null);
        cmsProductService.queryIsPromotion(cmsProduct);
        String userId = FrontSecurityUtils.getUserId();
        if (StringUtils.isNotEmpty(userId)&&!"0".equals(userId) ){
            umsBrowseHistoryService.asyncRecordBrowseHistory(userId,productId);
        }
        return AjaxResultResponse.success(cmsProduct);
    }




}
