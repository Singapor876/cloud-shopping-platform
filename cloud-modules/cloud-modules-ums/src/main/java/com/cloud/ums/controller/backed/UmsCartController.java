package com.cloud.ums.controller.backed;

import java.util.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.cms.domain.CmsProduct;
import com.cloud.cms.service.ICmsProductService;
import com.cloud.common.core.utils.poi.ExcelUtil;
import com.cloud.common.core.web.controller.BaseController;
import com.cloud.common.core.web.page.TableDataInfo;
import com.cloud.common.log.annotation.Log;
import com.cloud.common.log.enums.BusinessType;
import com.cloud.common.security.annotation.RequiresPermissions;
import com.cloud.ums.domain.AjaxResultResponse;
import com.cloud.ums.domain.UmsUser;
import com.cloud.ums.service.IUmsUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cloud.ums.domain.UmsCart;
import com.cloud.ums.service.IUmsCartService;

/**
 * Shopping Cart Controller
 * 
 * @author cloud
 * @date 2025-03-15
 */
@RestController
@RequestMapping("/cart")
public class UmsCartController extends BaseController {
    @Resource
    private IUmsCartService umsCartService;
    @Resource
    private IUmsUserService umsUserService;
    @Resource
    private ICmsProductService cmsProductService;

    /**
     * QueryShopping CartList
     */
    @RequiresPermissions("@ss.hasPermi('ums:cart:list')")
    @GetMapping("/list")
    public TableDataInfo list(UmsCart umsCart) {
        String userName = umsCart.getUserName();
        startPage();
        QueryWrapper<UmsCart> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("user_id", "GROUP_CONCAT(product_id ORDER BY update_time DESC) AS productIds",
                        "GROUP_CONCAT(quantity ORDER BY update_time DESC) AS quantityList")
                .groupBy("user_id");
        if (userName != null) {
            LambdaQueryWrapper<UmsUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.like(UmsUser::getUserName, userName);
            //Query条件
            UmsUser umsUser = umsUserService.getOne(lambdaQueryWrapper);
            if (umsUser != null) {
                queryWrapper.eq("user_id", umsUser.getUserId());
            }else{
                //无Data
                queryWrapper.eq("user_id", "0");
            }
        }
        List<UmsCart> list = umsCartService.list(queryWrapper);
        list.forEach(cart -> {
            cart.setUmsUser(umsUserService.getById(cart.getUserId()));
            // GetProductList（按 productIds Query）
            List<CmsProduct> productList = cmsProductService.listByIds(Arrays.asList(cart.getProductIds().split(",")));
            // Get数量List
            List<String> quantityList = Arrays.asList(cart.getQuantityList().split(","));
            // 确保两个List长度一致，避免索引越界
            if (productList.size() == quantityList.size()) {
                for (int i = 0; i < productList.size(); i++) {
                    productList.get(i).setQuantity(quantityList.get(i)); // 按索引赋值
                }
            } else {
                throw new RuntimeException("Product list and quantity list lengths do not match!");
            }
            cart.setCmsProductList(productList);
        });
        return getDataTable(list);
    }

    /**
     * ExportShopping CartList
     */
    @RequiresPermissions("@ss.hasPermi('ums:cart:export')")
    @Log(title = "Shopping Cart", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, UmsCart umsCart) {
        LambdaQueryWrapper<UmsCart> queryWrapper = new LambdaQueryWrapper<>(umsCart);
        queryWrapper.orderByDesc(UmsCart::getUpdateTime);
        List<UmsCart> list = umsCartService.list(queryWrapper);
        ExcelUtil<UmsCart> util = new ExcelUtil<UmsCart>(UmsCart.class);
        util.exportExcel(response, list, "Shopping CartData");
    }

    /**
     * GetShopping CartDetails
     */
    @RequiresPermissions("@ss.hasPermi('ums:cart:query')")
    @GetMapping(value = "/{id}")
    public AjaxResultResponse<UmsCart> getInfo(@PathVariable("id") String id) {
        return AjaxResultResponse.success(umsCartService.getById(id));
    }

    /**
     * AddShopping Cart
     */
    @RequiresPermissions("@ss.hasPermi('ums:cart:add')")
    @Log(title = "Shopping Cart", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResultResponse<Boolean> add(@RequestBody UmsCart cart) {
        String productId = cart.getProductId();
        String userId = cart.getUserId();
        if (productId == null || userId == null) {
            return AjaxResultResponse.error("ID cannot be empty", null);
        }
        LambdaQueryWrapper<UmsCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UmsCart::getUserId, userId)
                    .eq(UmsCart::getProductId, productId);
        UmsCart umsCart = umsCartService.getOne(queryWrapper);
        if (umsCart == null) {
            umsCart = new UmsCart();
            Date date = new Date();
            umsCart.setCreateTime(date);
            umsCart.setUpdateTime(date);
            return AjaxResultResponse.success(umsCartService.save(umsCart));
        }else{
            umsCart.setQuantity(umsCart.getQuantity() + cart.getQuantity());
            return AjaxResultResponse.success(umsCartService.updateById(umsCart));
        }
    }

    /**
     * UpdateShopping Cart
     */
    @RequiresPermissions("@ss.hasPermi('ums:cart:edit')")
    @Log(title = "Shopping Cart", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResultResponse<Boolean> edit(@RequestBody UmsCart umsCart) {
        umsCart.setUpdateTime(new Date());
        return AjaxResultResponse.success(umsCartService.updateById(umsCart));
    }

    /**
     * DeleteShopping Cart
     */
    @RequiresPermissions("@ss.hasPermi('ums:cart:remove')")
    @Log(title = "Shopping Cart", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResultResponse<Boolean> remove(@PathVariable List<String> ids) {
        for (String id : ids) {
            LambdaQueryWrapper<UmsCart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UmsCart::getUserId, id);
            umsCartService.remove(queryWrapper);
        }
        return AjaxResultResponse.success(true);
    }


}
