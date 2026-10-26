package com.cloud.ums.controller.backed;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloud.common.core.utils.poi.ExcelUtil;
import com.cloud.common.core.web.controller.BaseController;
import com.cloud.common.core.web.page.TableDataInfo;
import com.cloud.common.security.annotation.RequiresPermissions;
import com.cloud.ums.domain.AjaxResultResponse;
import com.cloud.ums.domain.UmsUser;
import com.cloud.ums.service.IUmsUserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * User Information Controller
 * 
 * @author cloud
 * @date 2025-01-11
 */
@RestController
@RequestMapping("/user")
public class UmsUserController extends BaseController {
    @Resource
    private IUmsUserService umsUserService;

    /**
     * QueryUser信息List
     */
    @RequiresPermissions("@ss.hasPermi('ums:user:list')")
    @GetMapping("/list")
    public TableDataInfo list(UmsUser umsUser) {
        startPage();
        LambdaQueryWrapper<UmsUser> queryWrapper = new LambdaQueryWrapper<>(umsUser);
        List<UmsUser> list = umsUserService.list(queryWrapper);
        return getDataTable(list);
    }

    /**
     * ExportUser信息List
     */
    @RequiresPermissions("@ss.hasPermi('ums:user:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, UmsUser umsUser) {
        LambdaQueryWrapper<UmsUser> queryWrapper = new LambdaQueryWrapper<>(umsUser);
        List<UmsUser> list = umsUserService.list(queryWrapper);
        ExcelUtil<UmsUser> util = new ExcelUtil<UmsUser>(UmsUser.class);
        util.exportExcel(response, list, "User Data");
    }

    /**
     * GetUser信息Details
     */
    @RequiresPermissions("@ss.hasPermi('ums:user:query')")
    @GetMapping(value = "/{userId}")
    public AjaxResultResponse<UmsUser> getInfo(@PathVariable("userId") String userId) {
        return AjaxResultResponse.success(umsUserService.getById(userId));
    }

    /**
     * AddUser信息
     */
    @RequiresPermissions("@ss.hasPermi('ums:user:add')")
    @PostMapping
    public AjaxResultResponse<Boolean> add(@RequestBody UmsUser umsUser) {
        Date date = new Date();
        umsUser.setCreateTime(date);
        umsUser.setUpdateTime(date);
        umsUser.setUserBalance(new BigDecimal(0));
        return AjaxResultResponse.success(umsUserService.save(umsUser));
    }

    /**
     * UpdateUser信息
     */
    @RequiresPermissions("@ss.hasPermi('ums:user:edit')")
    @PutMapping
    public AjaxResultResponse<Boolean> edit(@RequestBody UmsUser umsUser) {
        umsUser.setUpdateTime(new Date());
        return AjaxResultResponse.success(umsUserService.updateById(umsUser));
    }

    /**
     * DeleteUser信息
     */
    @RequiresPermissions("@ss.hasPermi('ums:user:remove')")
	@DeleteMapping("/{userIds}")
    public AjaxResultResponse<Boolean> remove(@PathVariable List<String> userIds) {
        return AjaxResultResponse.success((umsUserService.removeBatchByIds(userIds)));
    }
}
