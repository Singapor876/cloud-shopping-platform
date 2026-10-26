package com.cloud.ums.controller.front;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.cms.domain.CmsProduct;
import com.cloud.cms.service.ICmsProductService;
import com.cloud.common.core.utils.StringUtils;
import com.cloud.common.core.web.controller.BaseController;
import com.cloud.common.core.web.page.TableDataInfo;
import com.cloud.common.security.utils.FrontSecurityUtils;
import com.cloud.ums.domain.AjaxResultResponse;
import com.cloud.ums.domain.UmsCart;
import com.cloud.ums.domain.UmsUser;
import com.cloud.ums.service.IUmsCartService;
import com.cloud.ums.service.IUmsUserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Shopping Cart Controller
 * 
 * @author cloud
 * @date 2025-03-15
 */
@RestController
@RequestMapping("/front/cart")
public class UmsCartControllerFront extends BaseController {
    @Resource
    private IUmsCartService umsCartService;
    @Resource
    private IUmsUserService umsUserService;
    @Resource
    private ICmsProductService cmsProductService;

    /**
     * Query user shopping cart
     */
    @GetMapping("/list")
    public TableDataInfo list(UmsCart umsCart) {
        String userName = umsCart.getUserName();
        QueryWrapper<UmsCart> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("user_id", "GROUP_CONCAT(product_id ORDER BY update_time DESC) AS productIds",
                        "GROUP_CONCAT(quantity ORDER BY update_time DESC) AS quantityList")
                .groupBy("user_id").eq("user_id", FrontSecurityUtils.getUserId());
        if (userName != null) {
            LambdaQueryWrapper<UmsUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.like(UmsUser::getUserName, userName);
            UmsUser umsUser = umsUserService.getOne(lambdaQueryWrapper);
            if (umsUser != null) {
                queryWrapper.eq("user_id", umsUser.getUserId());
            }else{
                queryWrapper.eq("user_id", "0");
            }
        }
        List<UmsCart> list = umsCartService.list(queryWrapper);
        list.forEach(cart -> {
            cart.setUmsUser(umsUserService.getById(cart.getUserId()));
            List<CmsProduct> productList = cmsProductService.listByIds(Arrays.asList(cart.getProductIds().split(",")));
            productList.forEach(product -> {
                cmsProductService.queryIsPromotion(product);
            });
            List<String> quantityList = Arrays.asList(cart.getQuantityList().split(","));
            if (productList.size() == quantityList.size()) {
                for (int i = 0; i < productList.size(); i++) {
                    productList.get(i).setQuantity(quantityList.get(i));
                }
            } else {
                throw new RuntimeException("Product list and quantity list lengths do not match!");
            }
            cart.setCmsProductList(productList);
        });
        return getDataTable(list);
    }

    /**
     * Add product to cart
     */
    @PostMapping(value = "addProductInCart")
    public AjaxResultResponse<Boolean> addCart(@RequestBody UmsCart cart) {
        String productId = cart.getProductId();
        if (StringUtils.isBlank(productId)) {
            return AjaxResultResponse.error("ID cannot be empty", null);
        }
        String userId = FrontSecurityUtils.getUserId();
        cart.setUserId(userId);
        UmsCart umsCart = quantityNum(userId, productId);
        if (umsCart == null) {
            cart.setQuantity(1L);
            Date date = new Date();
            cart.setCreateTime(date);
            cart.setUpdateTime(date);
            return AjaxResultResponse.success(umsCartService.save(cart));
        } else if (umsCart.getQuantity() > 0L) {
            cart.setId(umsCart.getId());
            return AjaxResultResponse.success(umsCartService.updateById(cart));
        }
        return AjaxResultResponse.error("Invalid data", null);
    }

    /**
     * Remove product from cart
     */
    @PostMapping(value = "removeProductInCart")
    public AjaxResultResponse<Boolean> removeProductInCart(@RequestBody UmsCart cart) {
        String productId = cart.getProductId();
        if (StringUtils.isBlank(productId)) {
            return AjaxResultResponse.error("ID cannot be empty", null);
        }
        String userId = FrontSecurityUtils.getUserId();
        cart.setUserId(userId);
        UmsCart umsCart = quantityNum(userId, productId);
        Long currentQuantity = umsCart.getQuantity();
        cart.setId(umsCart.getId());
        if (currentQuantity > 1L) {
            return AjaxResultResponse.success(umsCartService.updateById(cart));
        } else if (currentQuantity == 1L) {
            return AjaxResultResponse.success("Delete successful", umsCartService.removeById(cart));
        }
        return AjaxResultResponse.error("Invalid data", null);
    }

    private UmsCart quantityNum(String userId, String productId) {
        LambdaQueryWrapper<UmsCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UmsCart::getUserId, userId)
                .eq(UmsCart::getProductId, productId);
        return umsCartService.getOne(queryWrapper);
    }



}
