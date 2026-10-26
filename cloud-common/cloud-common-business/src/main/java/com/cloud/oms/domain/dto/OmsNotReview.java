package com.cloud.oms.domain.dto;

import com.cloud.cms.domain.CmsProduct;
import lombok.Data;

import java.util.List;

@Data
public class OmsNotReview {

    private String orderId;

    private List<CmsProduct> cmsProductList;
}
