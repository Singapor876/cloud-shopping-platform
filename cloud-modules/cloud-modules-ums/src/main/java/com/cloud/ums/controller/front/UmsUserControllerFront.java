package com.cloud.ums.controller.front;

import cn.hutool.core.util.IdUtil;
import com.cloud.common.core.utils.StringUtils;
import com.cloud.common.security.utils.FrontSecurityUtils;
import com.cloud.common.security.utils.SecurityUtils;
import com.cloud.ums.domain.AjaxResultResponse;
import com.cloud.ums.domain.UmsUser;
import com.cloud.ums.domain.dto.UmsAppLoginDTO;
import com.cloud.ums.domain.dto.UmsAppRegisterDTO;
import com.cloud.ums.domain.vo.LoginVO;
import com.cloud.ums.service.IUmsUserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * Frontend - User Registration, Login, Logout Module
 */
//@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
@RestController
public class UmsUserControllerFront {

    @Resource
    private IUmsUserService umsUserService;

    /**
     * Register
     */
    @PostMapping("/register")
    public AjaxResultResponse<Boolean> register(@RequestBody UmsAppRegisterDTO umsAppRegisterDTO) {
        String username = umsAppRegisterDTO.getUsername();
        String password = umsAppRegisterDTO.getPassword();
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            return AjaxResultResponse.error("Username and password cannot be empty", null);
        }
        if (umsUserService.getUmsUserByUserName(username) != null) {
            return AjaxResultResponse.error("Username already exists", null);
        }
        UmsUser umsUser = new UmsUser();
        umsUser.setUserId(IdUtil.getSnowflakeNextIdStr());
        umsUser.setUserName(username);
        umsUser.setUserBalance(new BigDecimal(0));
        String encryptPassword = SecurityUtils.encryptPassword(password);
        umsUser.setUserPassword(encryptPassword);
        return AjaxResultResponse.success(umsUserService.save(umsUser));
    }

    /**
     * Login
     */
    @PostMapping("/login")
    public AjaxResultResponse<LoginVO> login(@RequestBody UmsAppLoginDTO umsAppLoginDTO) {
        String username = umsAppLoginDTO.getUsername();
        String password = umsAppLoginDTO.getPassword();
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            return AjaxResultResponse.error("Username and password cannot be empty", null);
        }
        umsAppLoginDTO.setUsername(username);
        umsAppLoginDTO.setPassword(password);
        return AjaxResultResponse.success(umsUserService.appLogin(umsAppLoginDTO));
    }

    /**
     * Logout
     */
    @PostMapping("/logout")
    public AjaxResultResponse<Boolean> logout(){
        return AjaxResultResponse.success(umsUserService.logout());
    }

    /**
     * Get personal information after login
     */
    @GetMapping("/getById")
    public AjaxResultResponse<UmsUser> getById(){
        return AjaxResultResponse.success(umsUserService.getById(FrontSecurityUtils.getUserId()));
    }

    /**
     * Update personal information after login
     */
    @PostMapping("/updatePersonalData")
    public AjaxResultResponse<Boolean> updatePersonalData(@RequestBody UmsUser umsUser) {
        String password = umsUser.getUserPassword();
        if (StringUtils.isNotBlank(password)&&password.length()<=16) {
            String encryptPassword = SecurityUtils.encryptPassword(password);
            umsUser.setUserPassword(encryptPassword);
        }
        umsUser.setUserId(FrontSecurityUtils.getUserId());
        return AjaxResultResponse.success(umsUserService.updateById(umsUser));
    }


}
