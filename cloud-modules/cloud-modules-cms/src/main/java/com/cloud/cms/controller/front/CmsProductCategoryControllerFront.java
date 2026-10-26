package com.cloud.cms.controller.front;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloud.cms.domain.CmsProduct;
import com.cloud.cms.domain.CmsProductCategory;
import com.cloud.cms.enums.ProductStatus;
import com.cloud.cms.service.ICmsProductCategoryService;
import com.cloud.cms.service.ICmsProductService;
import com.cloud.common.core.utils.StringUtils;
import com.cloud.common.core.web.controller.BaseController;
import com.cloud.ums.domain.AjaxResultResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * Product Category Controller
 * 
 * @author cloud
 * @date 2025-03-15
 */
@RestController
@RequestMapping("/front/category")
public class CmsProductCategoryControllerFront extends BaseController {
    @Resource
    private ICmsProductCategoryService cmsProductCategoryService;
    @Resource
    private ICmsProductService cmsProductService;

    /**
     * Query product category list
     */
    @GetMapping("/list")
    public AjaxResultResponse<List<CmsProductCategory>> list() {
        LambdaQueryWrapper<CmsProductCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(CmsProductCategory::getUpdateTime);
        List<CmsProductCategory> cmsProductCategoryList = cmsProductCategoryService.list(queryWrapper);
        return AjaxResultResponse.success(cmsProductCategoryService.buildTree(cmsProductCategoryList));
    }

    /**
     * Get product category details
     */
    @GetMapping(value = "/{id}")
    public AjaxResultResponse<CmsProductCategory> getInfo(@PathVariable("id") String categoryId) {
        if (StringUtils.isEmpty(categoryId)){
            return AjaxResultResponse.error("ID cannot be empty", null);
        }
        CmsProductCategory productCategory = cmsProductCategoryService.getById(categoryId);
        getProductByCategoryId(categoryId, productCategory);
        return AjaxResultResponse.success(productCategory);
    }

    private void getProductByCategoryId(@PathVariable("id") String categoryId, CmsProductCategory productCategory) {
        LambdaQueryWrapper<CmsProduct> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(CmsProduct::getCategoryId, categoryId)
                .eq(CmsProduct::getProductStatus, ProductStatus.UNSOLD.getValue());
        List<CmsProduct> cmsProductList = cmsProductService.list(lambdaQueryWrapper);
        productCategory.setCmsProductList(cmsProductList);
    }


}
