package com.cloud.oms.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OmsPlaceOrderProductItem {

    /** 商品ID */
    private String productId;
    /** 商品数量 */
    private Long quantity;
    /** 商品单价 */
    private BigDecimal price;
}
