package com.cloud.oms.controller.front;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloud.cms.domain.CmsProduct;
import com.cloud.cms.service.ICmsProductService;
import com.cloud.common.core.exception.ServiceException;
import com.cloud.common.core.utils.StringUtils;
import com.cloud.common.core.web.controller.BaseController;
import com.cloud.common.log.annotation.Log;
import com.cloud.common.log.enums.BusinessType;
import com.cloud.common.security.annotation.RequiresPermissions;
import com.cloud.common.security.utils.FrontSecurityUtils;
import com.cloud.oms.domain.OmsOrder;
import com.cloud.oms.domain.OmsOrderItem;
import com.cloud.oms.domain.dto.OmsPlaceOrder;
import com.cloud.oms.domain.dto.OmsPlaceOrderProductItem;
import com.cloud.oms.enums.OrderStatus;
import com.cloud.oms.service.IOmsOrderItemService;
import com.cloud.oms.service.IOmsOrderService;
import com.cloud.ums.domain.AjaxResultResponse;
import com.cloud.ums.domain.UmsCart;
import com.cloud.ums.domain.UmsUser;
import com.cloud.ums.service.IUmsCartService;
import com.cloud.ums.service.IUmsUserService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Order Controller
 * 
 * @author cloud
 * @date 2025-03-15
 */
@RestController
@RequestMapping("/front/order")
public class OmsOrderControllerFront extends BaseController {
    @Resource
    private IOmsOrderService omsOrderService;
    @Resource
    private IUmsUserService userService;
    @Resource
    private IOmsOrderItemService orderItemService;
    @Resource
    private ICmsProductService productService;
    @Resource
    private IUmsCartService cartService;

    /**
     * My orders
     */
    @GetMapping("/list")
    public AjaxResultResponse<List<OmsOrder>> list(OmsOrder omsOrder) {
        LambdaQueryWrapper<OmsOrder> queryWrapper = new LambdaQueryWrapper<>(omsOrder);
        queryWrapper.orderByDesc(OmsOrder::getUpdateTime)
                .eq(OmsOrder::getUserId, FrontSecurityUtils.getUserId());
        List<OmsOrder> list = omsOrderService.list(queryWrapper);
        list.forEach(order -> {
            String orderId = order.getId();
            OmsOrderControllerFront.getOrderItemByOrderId(orderId, order, orderItemService, productService, userService);
        });
        return AjaxResultResponse.success(list);
    }

    /**
     * Get order details
     */
    @GetMapping(value = "/{id}")
    public AjaxResultResponse<OmsOrder> getInfo(@PathVariable("id") String orderId) {
        if (StringUtils.isEmpty(orderId)){
            return AjaxResultResponse.error("ID cannot be empty", null);
        }
        OmsOrder order = omsOrderService.getById(orderId);
        getOrderItemByOrderId(orderId, order, orderItemService, productService, userService);
        return AjaxResultResponse.success(order);
    }

    public static void getOrderItemByOrderId(@PathVariable("id") String orderId, OmsOrder order, IOmsOrderItemService orderItemService, ICmsProductService productService, IUmsUserService userService) {
        LambdaQueryWrapper<OmsOrderItem> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(OmsOrderItem::getOrderId, orderId);
        List<OmsOrderItem> omsOrderItemList = orderItemService.list(lambdaQueryWrapper);
        omsOrderItemList.forEach(orderItem -> {
            String productId = orderItem.getProductId();
            orderItem.setCmsProduct(productService.getById(productId));
        });
        order.setOmsOrderItemList(omsOrderItemList);
        UmsUser umsUser = userService.getById(order.getUserId());
        if (umsUser!=null){
            order.setUserName(umsUser.getUserName());
        }
    }

    /**
     * Initialize order creation
     */
    @PostMapping("initOrder")
    @Transactional
    public AjaxResultResponse<OmsOrder> initOrder(@RequestBody OmsPlaceOrder omsPlaceOrder) {
        verify(omsPlaceOrder);
        OmsOrder order = new OmsOrder();
        String orderId = IdUtil.getSnowflakeNextIdStr();
        order.setId(orderId);
        order.setUserId(FrontSecurityUtils.getUserId());
        order.setUserAddress(omsPlaceOrder.getUserAddress());
        order.setTotalAmount(omsPlaceOrder.getTotalAmount());
        order.setPaymentMethod(omsPlaceOrder.getPaymentMethod());
        order.setOrderStatus(OrderStatus.UNPAYMENT.getValue());
        Date date = new Date();
        order.setCreateTime(date);
        order.setUpdateTime(date);
        omsOrderService.save(order);

        List<OmsOrderItem> omsOrderItemList = new ArrayList<>();
        for (OmsPlaceOrderProductItem item : omsPlaceOrder.getOmsPlaceOrderProductItemList()) {
            OmsOrderItem omsOrderItem = new OmsOrderItem();
            omsOrderItem.setId(IdUtil.getSnowflakeNextIdStr());
            omsOrderItem.setOrderId(orderId);
            omsOrderItem.setProductId(item.getProductId());
            omsOrderItem.setQuantity(item.getQuantity());
            omsOrderItem.setPrice(item.getPrice());
            omsOrderItem.setIsReview(false);
            omsOrderItem.setCreateTime(date);
            omsOrderItem.setUpdateTime(date);
            omsOrderItemList.add(omsOrderItem);
        }
        orderItemService.saveBatch(omsOrderItemList);

        return AjaxResultResponse.success(order);
    }

    /**
     * Place order
     */
    @PostMapping("placeOrder")
    @Transactional
    public AjaxResultResponse<Boolean> placeOrder(@RequestBody OmsPlaceOrder omsPlaceOrder) {
        verify(omsPlaceOrder);
        return AjaxResultResponse.success(createOrder(omsPlaceOrder));
    }


    public void verify(OmsPlaceOrder omsPlaceOrder) {
        List<OmsPlaceOrderProductItem> omsPlaceOrderProductItemList = omsPlaceOrder.getOmsPlaceOrderProductItemList();
        if (CollUtil.isEmpty(omsPlaceOrderProductItemList)) {
            throw new ServiceException("Product list cannot be empty when placing an order", 500);
        }

        String paymentMethod = omsPlaceOrder.getPaymentMethod();
        if (StringUtils.isEmpty(paymentMethod)) {
            throw new ServiceException("Payment method cannot be empty", 500);
        }

        BigDecimal totalAmount = omsPlaceOrder.getTotalAmount();
        if (totalAmount == null) {
            throw new ServiceException("Amount cannot be empty", 500);
        }
    }

    /**
     * Actual order logic
     */
    private boolean createOrder(OmsPlaceOrder omsPlaceOrder) {
        String orderId = omsPlaceOrder.getOrderId();
        OmsOrder order = omsOrderService.getById(orderId);
        if (order == null) {
            throw new ServiceException("Order does not exist", 500);
        }

        BigDecimal totalAmount = omsPlaceOrder.getTotalAmount();
        UmsUser umsUser = userService.getById(FrontSecurityUtils.getUserId());
        BigDecimal userBalance = umsUser.getUserBalance();
        if (totalAmount.compareTo(userBalance) > 0) {
            throw new ServiceException("Insufficient user balance", 500);
        }

        for (OmsPlaceOrderProductItem item : omsPlaceOrder.getOmsPlaceOrderProductItemList()) {
            String productId = item.getProductId();
            Long quantity = item.getQuantity();

            CmsProduct cmsProduct = productService.getById(productId);
            Long stock = cmsProduct.getStock();
            if (quantity.compareTo(stock) > 0) {
                throw new ServiceException(cmsProduct.getName() + "Insufficient stock, please remove this product and re-order", 500);
            }

            cmsProduct.setStock(stock - quantity);
            productService.updateById(cmsProduct);

            LambdaQueryWrapper<UmsCart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UmsCart::getUserId, FrontSecurityUtils.getUserId())
                    .eq(UmsCart::getProductId, productId);
            cartService.remove(queryWrapper);
        }

        umsUser.setUserBalance(userBalance.subtract(totalAmount));
        userService.updateById(umsUser);

        order.setOrderStatus(OrderStatus.PAID.getValue());
        order.setUserAddress(omsPlaceOrder.getUserAddress());
        omsOrderService.updateById(order);

        return true;
    }


    /**
     * Cancel order
     */
    @PutMapping
    public AjaxResultResponse<Boolean> cancelOrder(@RequestBody OmsOrder omsOrder) {
        omsOrder.setUpdateTime(new Date());
        return AjaxResultResponse.success(omsOrderService.updateById(omsOrder));
    }

}
