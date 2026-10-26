package com.cloud.cms.controller.backed;

import com.cloud.cms.domain.CmdRecommendWeight;
import com.cloud.cms.service.ICmdRecommendWeightService;
import com.cloud.common.core.web.controller.BaseController;
import com.cloud.common.log.annotation.Log;
import com.cloud.common.log.enums.BusinessType;
import com.cloud.common.security.annotation.RequiresPermissions;
import com.cloud.ums.domain.AjaxResultResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;

/**
 * Recommendation Weight ConfigurationController
 */
@RestController
@RequestMapping("/recommend/weight")
public class CmdRecommendWeightController extends BaseController {
    @Resource
    private ICmdRecommendWeightService recommendWeightService;

    /**
     * GetRecommendation Weight Configuration
     */
    // @RequiresPermissions("@ss.hasPermi('cms:recommend:query')")
    @GetMapping
    public AjaxResultResponse<CmdRecommendWeight> getInfo() {
        return AjaxResultResponse.success(recommendWeightService.getOrInitWeight());
    }

    /**
     * UpdateRecommendation Weight Configuration
     */
    // @RequiresPermissions("@ss.hasPermi('cms:recommend:edit')")
    @Log(title = "Recommendation Weight Configuration", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResultResponse<Boolean> edit(@RequestBody CmdRecommendWeight recommendWeight) {
        Date now = new Date();
        CmdRecommendWeight current = recommendWeightService.getById("1");
        if (current == null) {
            recommendWeight.setId("1");
            recommendWeight.setCreateTime(now);
            recommendWeight.setUpdateTime(now);
            return AjaxResultResponse.success(recommendWeightService.save(recommendWeight));
        }
        recommendWeight.setId("1");
        recommendWeight.setUpdateTime(now);
        return AjaxResultResponse.success(recommendWeightService.updateById(recommendWeight));
    }
}
