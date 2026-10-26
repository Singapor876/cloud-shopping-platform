package com.cloud.cms.controller.front;

import com.cloud.cms.domain.CmsProduct;
import com.cloud.cms.service.ICmdRecommendService;
import com.cloud.common.core.web.controller.BaseController;
import com.cloud.common.security.utils.FrontSecurityUtils;
import com.cloud.ums.domain.AjaxResultResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * Recommend Product Controller
 */
@RestController
@RequestMapping("/front/recommend")
public class CmdRecommendControllerFront extends BaseController {
    @Resource
    private ICmdRecommendService recommendService;

    /**
     * Get recommended product list
     */
    @GetMapping("/products")
    public AjaxResultResponse<List<CmsProduct>> products() {
        String userId = FrontSecurityUtils.getUserId();
        return AjaxResultResponse.success(recommendService.recommendProducts(userId));
    }
}