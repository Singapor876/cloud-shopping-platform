package com.cloud.ums.controller.front;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.cloud.cms.domain.CmsProduct;
import com.cloud.cms.service.ICmsProductService;
import com.cloud.common.core.utils.StringUtils;
import com.cloud.common.core.web.controller.BaseController;
import com.cloud.common.core.web.page.TableDataInfo;
import com.cloud.common.log.annotation.Log;
import com.cloud.common.log.enums.BusinessType;
import com.cloud.common.security.utils.FrontSecurityUtils;
import com.cloud.oms.domain.OmsOrder;
import com.cloud.oms.domain.OmsOrderItem;
import com.cloud.oms.domain.dto.OmsNotReview;
import com.cloud.oms.enums.LogisticsStatus;
import com.cloud.oms.enums.OrderStatus;
import com.cloud.oms.service.IOmsOrderItemService;
import com.cloud.oms.service.IOmsOrderService;
import com.cloud.ums.domain.AjaxResultResponse;
import com.cloud.ums.domain.UmsProductReview;
import com.cloud.ums.domain.UmsUser;
import com.cloud.ums.service.IUmsProductReviewService;
import com.cloud.ums.service.IUmsUserService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Product Review Controller
 * 
 * @author cloud
 * @date 2025-03-15
 */
@RestController
@RequestMapping("/front/review")
public class UmsProductReviewControllerFront extends BaseController {
    @Resource
    private IUmsProductReviewService umsProductReviewService;
    @Resource
    private IUmsUserService umsUserService;
    @Resource
    private ICmsProductService cmsProductService;
    @Resource
    private IOmsOrderService orderService;
    @Resource
    private IOmsOrderItemService orderItemService;

    /**
     * Pending reviews - product reviews after order completion
     */
    @GetMapping("/notReviewList")
    public TableDataInfo notReviewList() {
        List<OmsNotReview> omsNotReviewList = new ArrayList<>();
        LambdaQueryWrapper<OmsOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OmsOrder::getOrderStatus, OrderStatus.COMPLETE.getValue())
//                .eq(OmsOrder::getLogisticsStatus, LogisticsStatus.COMPLETE.getValue())
                .eq(OmsOrder::getUserId, FrontSecurityUtils.getUserId())
                .orderByDesc(OmsOrder::getUpdateTime);
        List<OmsOrder> omsOrderList = orderService.list(queryWrapper);
        if (CollUtil.isNotEmpty(omsOrderList)) {
            List<String> orderIdList = omsOrderList.stream()
                    .map(OmsOrder::getId)
                    .collect(Collectors.toList());
            if (CollUtil.isNotEmpty(orderIdList)) {
                LambdaQueryWrapper<OmsOrderItem> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.in(OmsOrderItem::getOrderId, orderIdList)
                        .eq(OmsOrderItem::getIsReview, false);
                List<OmsOrderItem> omsOrderItemList = orderItemService.list(lambdaQueryWrapper);
                if (CollUtil.isNotEmpty(omsOrderItemList)) {
                    List<String> productIdList = omsOrderItemList.stream()
                            .map(OmsOrderItem::getProductId)
                            .collect(Collectors.toList());

                    if (CollUtil.isNotEmpty(productIdList)) {
                        List<CmsProduct> productList = cmsProductService.listByIds(productIdList);
                        Map<String, List<CmsProduct>> orderProductMap = new HashMap<>();
                        for (OmsOrderItem omsOrderItem : omsOrderItemList) {
                            String orderId = omsOrderItem.getOrderId();
                            CmsProduct product = productList.stream()
                                    .filter(p -> p.getId().equals(omsOrderItem.getProductId()))
                                    .findFirst()
                                    .orElse(null);
                            if (product != null) {
                                orderProductMap.computeIfAbsent(orderId, k -> new ArrayList<>()).add(product);
                            }
                        }
                        for (Map.Entry<String, List<CmsProduct>> entry : orderProductMap.entrySet()) {
                            String orderId = entry.getKey();
                            List<CmsProduct> products = entry.getValue();
                            OmsNotReview omsNotReview = new OmsNotReview();
                            omsNotReview.setOrderId(orderId);
                            omsNotReview.setCmsProductList(products);
                            omsNotReviewList.add(omsNotReview);
                        }
                    }
                }
            }
        }
        return getDataTable(omsNotReviewList);
    }


    /**
     * Query product review list
     */
    @GetMapping("/list")
    public TableDataInfo list() {
        LambdaQueryWrapper<UmsProductReview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(UmsProductReview::getUpdateTime)
                .eq(UmsProductReview::getUserId, FrontSecurityUtils.getUserId());
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
     * Submit review - after order completion
     */
    @PostMapping(value = "addReview")
    @Transactional
    public AjaxResultResponse<Boolean> addReview(@RequestBody UmsProductReview umsProductReview) {
        String orderId = umsProductReview.getOrderId();
        String productId = umsProductReview.getProductId();
        if (StringUtils.isEmpty(orderId)) {
            return AjaxResultResponse.error("Order ID cannot be empty", null);
        }
        LambdaUpdateWrapper<OmsOrderItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(OmsOrderItem::getIsReview, true).eq(OmsOrderItem::getOrderId, orderId)
                .eq(OmsOrderItem::getProductId, productId);
        orderItemService.update(updateWrapper);
        umsProductReview.setUserId(FrontSecurityUtils.getUserId());
        Date date = new Date();
        umsProductReview.setCreateTime(date);
        umsProductReview.setUpdateTime(date);
        return AjaxResultResponse.success(umsProductReviewService.save(umsProductReview));
    }

    /**
     * Delete product review
     */
    @Log(title = "Product review", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResultResponse<Boolean> remove(@PathVariable List<String> ids) {
        return AjaxResultResponse.success((umsProductReviewService.removeBatchByIds(ids)));
    }


}
