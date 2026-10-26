package com.cloud.oms.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OmsPlaceOrder {

    private String orderId;
    /** 商品信息 */
    private List<OmsPlaceOrderProductItem> omsPlaceOrderProductItemList;
    /** 订单总金额 */
    private BigDecimal totalAmount;
    /** 支付方式 */
    private String paymentMethod;
    /** 用户收货地址 */
    private String userAddress;

}
