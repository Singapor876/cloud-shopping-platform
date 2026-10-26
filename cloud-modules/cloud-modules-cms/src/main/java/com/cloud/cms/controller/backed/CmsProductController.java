package com.cloud.cms.controller.backed;

import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloud.cms.domain.CmsProductCategory;
import com.cloud.cms.service.ICmsProductCategoryService;
import com.cloud.common.core.utils.StringUtils;
import com.cloud.common.core.utils.poi.ExcelUtil;
import com.cloud.common.core.web.controller.BaseController;
import com.cloud.common.core.web.page.TableDataInfo;
import com.cloud.common.log.annotation.Log;
import com.cloud.common.log.enums.BusinessType;
import com.cloud.common.security.annotation.RequiresPermissions;
import com.cloud.ums.domain.AjaxResultResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cloud.cms.domain.CmsProduct;
import com.cloud.cms.service.ICmsProductService;

/**
 * ProductController
 * 
 * @author cloud
 * @date 2025-03-15
 */
@RestController
@RequestMapping("/product")
public class CmsProductController extends BaseController {
    @Resource
    private ICmsProductService cmsProductService;
    @Resource
    private ICmsProductCategoryService cmsProductCategoryService;

    /**
     * QueryProductList
     */
    @RequiresPermissions("@ss.hasPermi('cms:product:list')")
    @GetMapping("/list")
    public TableDataInfo list(CmsProduct cmsProduct) {
        startPage();
        LambdaQueryWrapper<CmsProduct> queryWrapper = new LambdaQueryWrapper<>(cmsProduct);
        queryWrapper.orderByDesc(CmsProduct::getUpdateTime);
        List<CmsProduct> list = cmsProductService.list(queryWrapper);
        list.forEach(product -> {
            String categoryId = product.getCategoryId();
            if (StringUtils.isNotEmpty(categoryId)){
                CmsProductCategory productCategory = cmsProductCategoryService.getById(categoryId);
                if (productCategory!=null){
                    product.setCategoryName(productCategory.getName());
                }
            }
        });
        return getDataTable(list);
    }

    /**
     * ExportProductList
     */
    @RequiresPermissions("@ss.hasPermi('cms:product:export')")
    @Log(title = "Product", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CmsProduct cmsProduct) {
        LambdaQueryWrapper<CmsProduct> queryWrapper = new LambdaQueryWrapper<>(cmsProduct);
        queryWrapper.orderByDesc(CmsProduct::getUpdateTime);
        List<CmsProduct> list = cmsProductService.list(queryWrapper);
        ExcelUtil<CmsProduct> util = new ExcelUtil<CmsProduct>(CmsProduct.class);
        util.exportExcel(response, list, "Product Data");
    }

    /**
     * GetProductDetails
     */
    @RequiresPermissions("@ss.hasPermi('cms:product:query')")
    @GetMapping(value = "/{id}")
    public AjaxResultResponse<CmsProduct> getInfo(@PathVariable("id") String id) {
        return AjaxResultResponse.success(cmsProductService.getById(id));
    }

    /**
     * AddProduct
     */
    @RequiresPermissions("@ss.hasPermi('cms:product:add')")
    @Log(title = "Product", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResultResponse<Boolean> add(@RequestBody CmsProduct cmsProduct) {
        Date date = new Date();
        cmsProduct.setCreateTime(date);
        cmsProduct.setUpdateTime(date);
        return AjaxResultResponse.success(cmsProductService.save(cmsProduct));
    }

    /**
     * UpdateProduct
     */
    @RequiresPermissions("@ss.hasPermi('cms:product:edit')")
    @Log(title = "Product", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResultResponse<Boolean> edit(@RequestBody CmsProduct cmsProduct) {
        cmsProduct.setUpdateTime(new Date());
        return AjaxResultResponse.success(cmsProductService.updateById(cmsProduct));
    }

    /**
     * DeleteProduct
     */
    @RequiresPermissions("@ss.hasPermi('cms:product:remove')")
    @Log(title = "Product", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResultResponse<Boolean> remove(@PathVariable List<String> ids) {
        return AjaxResultResponse.success((cmsProductService.removeBatchByIds(ids)));
    }
}
