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
import com.cloud.ums.domain.UmsBrowseHistory;
import com.cloud.ums.service.IUmsBrowseHistoryService;

/**
 * Browse History Controller
 * 
 * @author cloud
 * @date 2025-03-15
 */
@RestController
@RequestMapping("/history")
public class UmsBrowseHistoryController extends BaseController {
    @Resource
    private IUmsBrowseHistoryService umsBrowseHistoryService;
    @Resource
    private IUmsUserService umsUserService;
    @Resource
    private ICmsProductService cmsProductService;

    /**
     * Query browse history list
     */
    @RequiresPermissions("@ss.hasPermi('ums:history:list')")
    @GetMapping("/list")
    public TableDataInfo list(UmsBrowseHistory umsBrowseHistory) {
        startPage();
        LambdaQueryWrapper<UmsBrowseHistory> queryWrapper = new LambdaQueryWrapper<>(umsBrowseHistory);
        queryWrapper.orderByDesc(UmsBrowseHistory::getBrowseTime);
        List<UmsBrowseHistory> list = umsBrowseHistoryService.list(queryWrapper);
        list.forEach(browseHistory -> {
            String userId = browseHistory.getUserId();
            if (StringUtils.isNotEmpty(userId)) {
                UmsUser user = umsUserService.getById(userId);
                if (user != null) {
                    browseHistory.setUserName(user.getUserName());
                }
            }
            String productId = browseHistory.getProductId();
            if (StringUtils.isNotEmpty(productId)) {
                CmsProduct product = cmsProductService.getById(productId);
                if (product != null) {
                    browseHistory.setProductImage(product.getImageUrl());
                    browseHistory.setProductName(product.getName());
                }
            }
        });
        return getDataTable(list);
    }

    /**
     * Export browse history list
     */
    @RequiresPermissions("@ss.hasPermi('ums:history:export')")
    @Log(title = "Browse history", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, UmsBrowseHistory umsBrowseHistory) {
        LambdaQueryWrapper<UmsBrowseHistory> queryWrapper = new LambdaQueryWrapper<>(umsBrowseHistory);
        queryWrapper.orderByDesc(UmsBrowseHistory::getBrowseTime);
        List<UmsBrowseHistory> list = umsBrowseHistoryService.list(queryWrapper);
        ExcelUtil<UmsBrowseHistory> util = new ExcelUtil<UmsBrowseHistory>(UmsBrowseHistory.class);
        util.exportExcel(response, list, "Browse History Data");
    }

    /**
     * Get browse history details
     */
    @RequiresPermissions("@ss.hasPermi('ums:history:query')")
    @GetMapping(value = "/{id}")
    public AjaxResultResponse<UmsBrowseHistory> getInfo(@PathVariable("id") String id) {
        return AjaxResultResponse.success(umsBrowseHistoryService.getById(id));
    }

    /**
     * Add browse history
     */
    @RequiresPermissions("@ss.hasPermi('ums:history:add')")
    @Log(title = "Browse history", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResultResponse<Boolean> add(@RequestBody UmsBrowseHistory umsBrowseHistory) {
        Date date = new Date();
        umsBrowseHistory.setBrowseTime(date);
        return AjaxResultResponse.success(umsBrowseHistoryService.save(umsBrowseHistory));
    }

    /**
     * Update browse history
     */
    @RequiresPermissions("@ss.hasPermi('ums:history:edit')")
    @Log(title = "Browse history", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResultResponse<Boolean> edit(@RequestBody UmsBrowseHistory umsBrowseHistory) {
        umsBrowseHistory.setBrowseTime(new Date());
        return AjaxResultResponse.success(umsBrowseHistoryService.updateById(umsBrowseHistory));
    }

    /**
     * Delete browse history
     */
    @RequiresPermissions("@ss.hasPermi('ums:history:remove')")
    @Log(title = "Browse history", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResultResponse<Boolean> remove(@PathVariable List<String> ids) {
        return AjaxResultResponse.success((umsBrowseHistoryService.removeBatchByIds(ids)));
    }
}
