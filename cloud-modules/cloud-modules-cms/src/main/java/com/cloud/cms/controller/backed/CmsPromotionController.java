package com.cloud.cms.controller.backed;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloud.cms.domain.CmsProductPromotion;
import com.cloud.cms.domain.CmsPromotion;
import com.cloud.cms.domain.dto.CmsDistributeProduct;
import com.cloud.cms.service.ICmsProductPromotionService;
import com.cloud.cms.service.ICmsProductService;
import com.cloud.cms.service.ICmsPromotionService;
import com.cloud.common.core.exception.ServiceException;
import com.cloud.common.core.utils.StringUtils;
import com.cloud.common.core.utils.poi.ExcelUtil;
import com.cloud.common.core.web.controller.BaseController;
import com.cloud.common.core.web.page.TableDataInfo;
import com.cloud.common.log.annotation.Log;
import com.cloud.common.log.enums.BusinessType;
import com.cloud.common.security.annotation.RequiresPermissions;
import com.cloud.ums.domain.AjaxResultResponse;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PromotionActivityController
 * 
 * @author cloud
 * @date 2025-03-15
 */
@RestController
@RequestMapping("/promotion")
public class CmsPromotionController extends BaseController {
    @Resource
    private ICmsPromotionService cmsPromotionService;
    @Resource
    private ICmsProductPromotionService cmsProductPromotionService;
    @Resource
    private ICmsProductService cmsProductService;

    /**
     * QueryPromotionActivityList
     */
    @RequiresPermissions("@ss.hasPermi('cms:promotion:list')")
    @GetMapping("/list")
    public TableDataInfo list(CmsPromotion cmsPromotion) {
        startPage();
        LambdaQueryWrapper<CmsPromotion> queryWrapper = new LambdaQueryWrapper<>(cmsPromotion);
        queryWrapper.orderByDesc(CmsPromotion::getUpdateTime);
        List<CmsPromotion> list = cmsPromotionService.list(queryWrapper);
        //把每个Activity的Product Data组装上去
        list.forEach(promotion -> {
            String promotionId = promotion.getId();
            LambdaQueryWrapper<CmsProductPromotion> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(CmsProductPromotion::getPromotionId, promotionId);
            List<CmsProductPromotion> productPromotionList = cmsProductPromotionService.list(lambdaQueryWrapper);
            if (CollUtil.isNotEmpty(productPromotionList)){
                List<String> productIdList = productPromotionList.stream().map(CmsProductPromotion::getProductId).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(productIdList)){
                    promotion.setProductList(cmsProductService.listByIds(productIdList));
                }
            }
        });
        return getDataTable(list);
    }

    /**
     * tospecifiedActivityassignProduct
     */
    @Log(title = "tospecifiedActivityassignProduct", businessType = BusinessType.UPDATE)
    @PostMapping("distributeProductByPromotionId")
    public AjaxResultResponse<Boolean> distributeProductByPromotionId(@RequestBody CmsDistributeProduct cmsDistributeProduct) {
        String promotionId = cmsDistributeProduct.getPromotionId();
        List<String> productIds = cmsDistributeProduct.getProductIds();
        if (StringUtils.isBlank(promotionId)||CollUtil.isEmpty(productIds)){
            return AjaxResultResponse.error("ID cannot be empty", null);
        }
        List<CmsProductPromotion> cmsProductPromotionList = new ArrayList<>();
        for (String productId : productIds){
            //校验该Product是否已被Assign promotion
            LambdaQueryWrapper<CmsProductPromotion> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(CmsProductPromotion::getProductId, productId);
            List<CmsProductPromotion> cmsProductPromotions = cmsProductPromotionService.list(lambdaQueryWrapper);
            if (CollUtil.isNotEmpty(cmsProductPromotions)){
                throw new ServiceException(cmsProductService.getById(productId).getName()+"Already assigned promotion, please uncheck");
            }
            CmsProductPromotion cmsProductPromotion = new CmsProductPromotion();
            cmsProductPromotion.setId(IdUtil.getSnowflakeNextIdStr());
            cmsProductPromotion.setPromotionId(promotionId);
            cmsProductPromotion.setProductId(productId);
            Date date = new Date();
            cmsProductPromotion.setCreateTime(date);
            cmsProductPromotion.setUpdateTime(date);
            cmsProductPromotionList.add(cmsProductPromotion);
        }
        //先删后加
        LambdaQueryWrapper<CmsProductPromotion> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(CmsProductPromotion::getPromotionId, promotionId);
        List<CmsProductPromotion> productPromotionList = cmsProductPromotionService.list(lambdaQueryWrapper);
        cmsProductPromotionService.removeBatchByIds(productPromotionList);
        return AjaxResultResponse.success(cmsProductPromotionService.saveBatch(cmsProductPromotionList));
    }

    /**
     * ExportPromotionActivityList
     */
    @RequiresPermissions("@ss.hasPermi('cms:promotion:export')")
    @Log(title = "PromotionActivity", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CmsPromotion cmsPromotion) {
        LambdaQueryWrapper<CmsPromotion> queryWrapper = new LambdaQueryWrapper<>(cmsPromotion);
        queryWrapper.orderByDesc(CmsPromotion::getUpdateTime);
        List<CmsPromotion> list = cmsPromotionService.list(queryWrapper);
        ExcelUtil<CmsPromotion> util = new ExcelUtil<CmsPromotion>(CmsPromotion.class);
        util.exportExcel(response, list, "PromotionActivityData");
    }

    /**
     * GetPromotionActivityDetails
     */
    @RequiresPermissions("@ss.hasPermi('cms:promotion:query')")
    @GetMapping(value = "/{id}")
    public AjaxResultResponse<CmsPromotion> getInfo(@PathVariable("id") String id) {
        return AjaxResultResponse.success(cmsPromotionService.getById(id));
    }

    /**
     * AddPromotionActivity
     */
    @RequiresPermissions("@ss.hasPermi('cms:promotion:add')")
    @Log(title = "PromotionActivity", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResultResponse<Boolean> add(@RequestBody CmsPromotion cmsPromotion) {
        Date date = new Date();
        cmsPromotion.setCreateTime(date);
        cmsPromotion.setUpdateTime(date);
        return AjaxResultResponse.success(cmsPromotionService.save(cmsPromotion));
    }

    /**
     * UpdatePromotionActivity
     */
    @RequiresPermissions("@ss.hasPermi('cms:promotion:edit')")
    @Log(title = "PromotionActivity", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResultResponse<Boolean> edit(@RequestBody CmsPromotion cmsPromotion) {
        cmsPromotion.setUpdateTime(new Date());
        return AjaxResultResponse.success(cmsPromotionService.updateById(cmsPromotion));
    }

    /**
     * DeletePromotionActivity
     */
    @RequiresPermissions("@ss.hasPermi('cms:promotion:remove')")
    @Log(title = "PromotionActivity", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResultResponse<Boolean> remove(@PathVariable List<String> ids) {
        return AjaxResultResponse.success((cmsPromotionService.removeBatchByIds(ids)));
    }
}
