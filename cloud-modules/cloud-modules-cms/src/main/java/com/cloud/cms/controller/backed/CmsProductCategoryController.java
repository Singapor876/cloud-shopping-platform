package com.cloud.cms.controller.backed;

import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.cloud.cms.domain.CmsProduct;
import com.cloud.cms.service.ICmsProductService;
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
import com.cloud.cms.domain.CmsProductCategory;
import com.cloud.cms.service.ICmsProductCategoryService;

/**
 * Product CategoryController
 * 
 * @author cloud
 * @date 2025-03-15
 */
@RestController
@RequestMapping("/category")
public class CmsProductCategoryController extends BaseController {
    @Resource
    private ICmsProductCategoryService cmsProductCategoryService;
    @Resource
    private ICmsProductService cmsProductService;

    /**
     * QueryProduct CategoryList
     */
    @RequiresPermissions("@ss.hasPermi('cms:category:list')")
    @GetMapping("/list")
    public TableDataInfo list(CmsProductCategory cmsProductCategory) {
        PageHelper.startPage(1,9999);
        LambdaQueryWrapper<CmsProductCategory> queryWrapper = new LambdaQueryWrapper<>(cmsProductCategory);
        queryWrapper.orderByDesc(CmsProductCategory::getUpdateTime);
        List<CmsProductCategory> cmsProductCategoryList = cmsProductCategoryService.list(queryWrapper);
        cmsProductCategoryList.forEach(productCategory -> {
            String categoryId = productCategory.getId();
            LambdaQueryWrapper<CmsProduct> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(CmsProduct::getCategoryId, categoryId);
            List<CmsProduct> cmsProductList = cmsProductService.list(lambdaQueryWrapper);
            productCategory.setCmsProductList(cmsProductList);
        });
        List<CmsProductCategory> cmsProductCategoryListTree = cmsProductCategoryService.buildTree(cmsProductCategoryList);
        return getDataTable(cmsProductCategoryListTree);
    }

    /**
     * GetLeaf NodeData
     */
    @GetMapping("/listLeafNodes")
    public AjaxResultResponse<List<CmsProductCategory>> listLeafNodes() {
        LambdaQueryWrapper<CmsProductCategory> queryWrapper = new LambdaQueryWrapper<>();
        List<CmsProductCategory> cmsProductCategoryList = cmsProductCategoryService.list(queryWrapper);
        List<CmsProductCategory> cmsProductCategoryListLeafNodes = cmsProductCategoryService.getAllLeafNodes(cmsProductCategoryList);
        return AjaxResultResponse.success(cmsProductCategoryListLeafNodes);
    }

    /**
     * ExportProduct CategoryList
     */
    @RequiresPermissions("@ss.hasPermi('cms:category:export')")
    @Log(title = "Product Category", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CmsProductCategory cmsProductCategory) {
        LambdaQueryWrapper<CmsProductCategory> queryWrapper = new LambdaQueryWrapper<>(cmsProductCategory);
        queryWrapper.orderByDesc(CmsProductCategory::getUpdateTime);
        List<CmsProductCategory> list = cmsProductCategoryService.list(queryWrapper);
        ExcelUtil<CmsProductCategory> util = new ExcelUtil<CmsProductCategory>(CmsProductCategory.class);
        util.exportExcel(response, list, "Product Category Data");
    }

    /**
     * GetProduct CategoryDetails
     */
    @RequiresPermissions("@ss.hasPermi('cms:category:query')")
    @GetMapping(value = "/{id}")
    public AjaxResultResponse<CmsProductCategory> getInfo(@PathVariable("id") String id) {
        return AjaxResultResponse.success(cmsProductCategoryService.getById(id));
    }

    /**
     * AddProduct Category
     */
    @RequiresPermissions("@ss.hasPermi('cms:category:add')")
    @Log(title = "Product Category", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResultResponse<Boolean> add(@RequestBody CmsProductCategory cmsProductCategory) {
        Date date = new Date();
        cmsProductCategory.setCreateTime(date);
        cmsProductCategory.setUpdateTime(date);
        return AjaxResultResponse.success(cmsProductCategoryService.save(cmsProductCategory));
    }

    /**
     * UpdateProduct Category
     */
    @RequiresPermissions("@ss.hasPermi('cms:category:edit')")
    @Log(title = "Product Category", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResultResponse<Boolean> edit(@RequestBody CmsProductCategory cmsProductCategory) {
        cmsProductCategory.setUpdateTime(new Date());
        return AjaxResultResponse.success(cmsProductCategoryService.updateById(cmsProductCategory));
    }

    /**
     * DeleteProduct Category
     */
    @RequiresPermissions("@ss.hasPermi('cms:category:remove')")
    @Log(title = "Product Category", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResultResponse<Boolean> remove(@PathVariable List<String> ids) {
        return AjaxResultResponse.success((cmsProductCategoryService.removeBatchByIds(ids)));
    }
}
