package com.cloud.cms.controller.front;

import com.cloud.cms.domain.CmdIndexContent;
import com.cloud.cms.service.ICmdIndexContentService;
import com.cloud.common.core.web.controller.BaseController;
import com.cloud.ums.domain.AjaxResultResponse;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * Home Content Controller
 */
@RestController
@RequestMapping("/front/content")
public class CmdIndexContentControllerFront extends BaseController {
    @Resource
    private ICmdIndexContentService cmdIndexContentService;

    /**
     * Get home content details
     */
    @GetMapping
    public AjaxResultResponse<CmdIndexContent> getInfo() {
        return AjaxResultResponse.success(cmdIndexContentService.getById(1));
    }


}
