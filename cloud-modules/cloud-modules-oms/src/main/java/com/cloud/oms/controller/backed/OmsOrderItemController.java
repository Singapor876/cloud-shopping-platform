package com.cloud.oms.controller.backed;

import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.cloud.oms.domain.OmsOrderItem;
import com.cloud.oms.service.IOmsOrderItemService;

/**
 * OrderProductController
 * 
 * @author cloud
 * @date 2025-03-15
 */
@RestController
@RequestMapping("/item")
public class OmsOrderItemController extends BaseController {
    @Resource
    private IOmsOrderItemService omsOrderItemService;

    /**
     * QueryOrderProductList
     */
    @RequiresPermissions("@ss.hasPermi('oms:item:list')")
    @GetMapping("/list")
    public TableDataInfo list(OmsOrderItem omsOrderItem) {
        startPage();
        LambdaQueryWrapper<OmsOrderItem> queryWrapper = new LambdaQueryWrapper<>(omsOrderItem);
        List<OmsOrderItem> list = omsOrderItemService.list(queryWrapper);
        return getDataTable(list);
    }

    /**
     * ExportOrderProductList
     */
    @RequiresPermissions("@ss.hasPermi('oms:item:export')")
    @Log(title = "OrderProduct", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, OmsOrderItem omsOrderItem) {
        LambdaQueryWrapper<OmsOrderItem> queryWrapper = new LambdaQueryWrapper<>(omsOrderItem);
        queryWrapper.orderByDesc(OmsOrderItem::getUpdateTime);
        List<OmsOrderItem> list = omsOrderItemService.list(queryWrapper);
        ExcelUtil<OmsOrderItem> util = new ExcelUtil<OmsOrderItem>(OmsOrderItem.class);
        util.exportExcel(response, list, "OrderProduct Data");
    }

    /**
     * GetOrderProductDetails
     */
    @RequiresPermissions("@ss.hasPermi('oms:item:query')")
    @GetMapping(value = "/{id}")
    public AjaxResultResponse<OmsOrderItem> getInfo(@PathVariable("id") String id) {
        return AjaxResultResponse.success(omsOrderItemService.getById(id));
    }

    /**
     * AddOrderProduct
     */
    @RequiresPermissions("@ss.hasPermi('oms:item:add')")
    @Log(title = "OrderProduct", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResultResponse<Boolean> add(@RequestBody OmsOrderItem omsOrderItem) {
        Date date = new Date();
        omsOrderItem.setCreateTime(date);
        omsOrderItem.setUpdateTime(date);
        return AjaxResultResponse.success(omsOrderItemService.save(omsOrderItem));
    }

    /**
     * UpdateOrderProduct
     */
    @RequiresPermissions("@ss.hasPermi('oms:item:edit')")
    @Log(title = "OrderProduct", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResultResponse<Boolean> edit(@RequestBody OmsOrderItem omsOrderItem) {
        omsOrderItem.setUpdateTime(new Date());
        return AjaxResultResponse.success(omsOrderItemService.updateById(omsOrderItem));
    }

    /**
     * DeleteOrderProduct
     */
    @RequiresPermissions("@ss.hasPermi('oms:item:remove')")
    @Log(title = "OrderProduct", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResultResponse<Boolean> remove(@PathVariable List<String> ids) {
        return AjaxResultResponse.success((omsOrderItemService.removeBatchByIds(ids)));
    }
}
