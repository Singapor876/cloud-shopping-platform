package com.cloud.ums.controller.backed;

import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloud.cms.domain.CmsProduct;
import com.cloud.cms.service.ICmsProductService;
import com.cloud.common.core.utils.StringUtils;
import com.cloud.common.core.utils.poi.ExcelUtil;
import com.cloud.common.core.web.controller.BaseController;
import com.cloud.common.core.web.page.TableDataInfo;
import com.cloud.common.log.annotation.Log;
import com.cloud.common.log.enums.BusinessType;
import com.cloud.common.security.annotation.RequiresPermissions;
import com.cloud.ums.domain.AjaxResultResponse;
import com.cloud.ums.domain.UmsUser;
import com.cloud.ums.service.IUmsUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cloud.ums.domain.UmsProductReview;
import com.cloud.ums.service.IUmsProductReviewService;

/**
 * Product Review Controller
 * 
 * @author cloud
 * @date 2025-03-15
 */
@RestController
@RequestMapping("/review")
public class UmsProductReviewController extends BaseController {
    @Resource
    private IUmsProductReviewService umsProductReviewService;
    @Resource
    private IUmsUserService umsUserService;
    @Resource
    private ICmsProductService cmsProductService;

    /**
     * Query product review list
     */
    @RequiresPermissions("@ss.hasPermi('ums:review:list')")
    @GetMapping("/list")
    public TableDataInfo list(UmsProductReview umsProductReview) {
        startPage();
        LambdaQueryWrapper<UmsProductReview> queryWrapper = new LambdaQueryWrapper<>(umsProductReview);
        queryWrapper.orderByDesc(UmsProductReview::getUpdateTime);
        List<UmsProductReview> list = umsProductReviewService.list(queryWrapper);
        list.forEach(productReview -> {
            String userId = productReview.getUserId();
            if (StringUtils.isNotEmpty(userId)) {
                UmsUser user = umsUserService.getById(userId);
                if (user != null) {
                    productReview.setUserName(user.getUserName());
                }
            }
            String productId = productReview.getProductId();
            if (StringUtils.isNotEmpty(productId)) {
                CmsProduct product = cmsProductService.getById(productId);
                if (product != null) {
                    productReview.setProductImage(product.getImageUrl());
                    productReview.setProductName(product.getName());
                }
            }
        });
        return getDataTable(list);
    }

    /**
     * Export product review list
     */
    @RequiresPermissions("@ss.hasPermi('ums:review:export')")
    @Log(title = "Product Review", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, UmsProductReview umsProductReview) {
        LambdaQueryWrapper<UmsProductReview> queryWrapper = new LambdaQueryWrapper<>(umsProductReview);
        queryWrapper.orderByDesc(UmsProductReview::getUpdateTime);
        List<UmsProductReview> list = umsProductReviewService.list(queryWrapper);
        ExcelUtil<UmsProductReview> util = new ExcelUtil<UmsProductReview>(UmsProductReview.class);
        util.exportExcel(response, list, "Product Review Data");
    }

    /**
     * Get product review details
     */
    @RequiresPermissions("@ss.hasPermi('ums:review:query')")
    @GetMapping(value = "/{id}")
    public AjaxResultResponse<UmsProductReview> getInfo(@PathVariable("id") String id) {
        return AjaxResultResponse.success(umsProductReviewService.getById(id));
    }

    /**
     * Add product review
     */
    @RequiresPermissions("@ss.hasPermi('ums:review:add')")
    @Log(title = "Product Review", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResultResponse<Boolean> add(@RequestBody UmsProductReview umsProductReview) {
        Date date = new Date();
        umsProductReview.setCreateTime(date);
        umsProductReview.setUpdateTime(date);
        return AjaxResultResponse.success(umsProductReviewService.save(umsProductReview));
    }

    /**
     * Update product review
     */
    @RequiresPermissions("@ss.hasPermi('ums:review:edit')")
    @Log(title = "Product Review", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResultResponse<Boolean> edit(@RequestBody UmsProductReview umsProductReview) {
        umsProductReview.setUpdateTime(new Date());
        return AjaxResultResponse.success(umsProductReviewService.updateById(umsProductReview));
    }

    /**
     * Delete product review
     */
    @RequiresPermissions("@ss.hasPermi('ums:review:remove')")
    @Log(title = "Product Review", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResultResponse<Boolean> remove(@PathVariable List<String> ids) {
        return AjaxResultResponse.success((umsProductReviewService.removeBatchByIds(ids)));
    }
}
