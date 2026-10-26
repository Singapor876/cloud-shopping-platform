package com.cloud.oms.controller.backed;

import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloud.cms.service.ICmsProductService;
import com.cloud.common.core.utils.poi.ExcelUtil;
import com.cloud.common.core.web.controller.BaseController;
import com.cloud.common.core.web.page.TableDataInfo;
import com.cloud.common.log.annotation.Log;
import com.cloud.common.log.enums.BusinessType;
import com.cloud.common.security.annotation.RequiresPermissions;
import com.cloud.oms.controller.front.OmsOrderControllerFront;
import com.cloud.oms.service.IOmsOrderItemService;
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
import com.cloud.oms.domain.OmsOrder;
import com.cloud.oms.service.IOmsOrderService;

/**
 * OrderController
 * 
 * @author cloud
 * @date 2025-03-15
 */
@RestController
@RequestMapping("/order")
public class OmsOrderController extends BaseController {
    @Resource
    private IOmsOrderService omsOrderService;
    @Resource
    private IUmsUserService userService;
    @Resource
    private IOmsOrderItemService orderItemService;
    @Resource
    private ICmsProductService productService;

    /**
     * QueryOrderList
     */
    @RequiresPermissions("@ss.hasPermi('oms:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(OmsOrder omsOrder) {
        startPage();
        LambdaQueryWrapper<OmsOrder> queryWrapper = new LambdaQueryWrapper<>(omsOrder);
        queryWrapper.orderByDesc(OmsOrder::getUpdateTime);
        List<OmsOrder> list = omsOrderService.list(queryWrapper);
        list.forEach(order -> {
            String orderId = order.getId();
            OmsOrderControllerFront.getOrderItemByOrderId(orderId, order, orderItemService, productService, userService);
        });
        return getDataTable(list);
    }

    /**
     * DropdownQueryUser
     */
    @GetMapping("/listUser")
    public AjaxResultResponse<List<UmsUser>> listUser() {
        return AjaxResultResponse.success(userService.list());
    }

    /**
     * ExportOrderList
     */
    @RequiresPermissions("@ss.hasPermi('oms:order:export')")
    @Log(title = "Order", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, OmsOrder omsOrder) {
        LambdaQueryWrapper<OmsOrder> queryWrapper = new LambdaQueryWrapper<>(omsOrder);
        queryWrapper.orderByDesc(OmsOrder::getUpdateTime);
        List<OmsOrder> list = omsOrderService.list(queryWrapper);
        ExcelUtil<OmsOrder> util = new ExcelUtil<OmsOrder>(OmsOrder.class);
        util.exportExcel(response, list, "Order Data");
    }

    /**
     * GetOrderDetails
     */
    @RequiresPermissions("@ss.hasPermi('oms:order:query')")
    @GetMapping(value = "/{id}")
    public AjaxResultResponse<OmsOrder> getInfo(@PathVariable("id") String id) {
        return AjaxResultResponse.success(omsOrderService.getById(id));
    }

    /**
     * AddOrder
     */
    @RequiresPermissions("@ss.hasPermi('oms:order:add')")
    @Log(title = "Order", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResultResponse<Boolean> add(@RequestBody OmsOrder omsOrder) {
        Date date = new Date();
        omsOrder.setCreateTime(date);
        omsOrder.setUpdateTime(date);
        return AjaxResultResponse.success(omsOrderService.save(omsOrder));
    }

    /**
     * UpdateOrder
     */
    @RequiresPermissions("@ss.hasPermi('oms:order:edit')")
    @Log(title = "Order", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResultResponse<Boolean> edit(@RequestBody OmsOrder omsOrder) {
        omsOrder.setUpdateTime(new Date());
        return AjaxResultResponse.success(omsOrderService.updateById(omsOrder));
    }

    /**
     * DeleteOrder
     */
    @RequiresPermissions("@ss.hasPermi('oms:order:remove')")
    @Log(title = "Order", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResultResponse<Boolean> remove(@PathVariable List<String> ids) {
        return AjaxResultResponse.success((omsOrderService.removeBatchByIds(ids)));
    }
}
