package com.cloud.ums.domain.dto;


import com.cloud.ums.domain.UmsCart;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 购物车对象 ums_cart
 * 
 * @author cloud
 * @date 2025-03-15
 */
@Data
public class UmsCartDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 购物车集合 */
    private List<UmsCart> umsCartList;
}
