package com.cloud.cms.controller.backed;

import com.cloud.cms.domain.vo.CmsDashboardSummary;
import com.cloud.cms.service.ICmsDashboardService;
import com.cloud.common.core.web.controller.BaseController;
import com.cloud.ums.domain.AjaxResultResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Backend Dashboard Statistics PanelController
 */
@RestController
@RequestMapping("/dashboard")
public class CmsDashboardController extends BaseController {
    @Resource
    private ICmsDashboardService cmsDashboardService;

    @GetMapping("/summary")
    public AjaxResultResponse<CmsDashboardSummary> summary() {
        return AjaxResultResponse.success(cmsDashboardService.getSummary());
    }
}
