package com.cloud.ums.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.ums.domain.UmsUser;
import com.cloud.ums.domain.dto.UmsAppLoginDTO;
import com.cloud.ums.domain.dto.UmsAppRegisterDTO;
import com.cloud.ums.domain.vo.LoginVO;

/**
 * 平台用户Service接口
 *
 * @author haiziohhue
 * @date 2024-10-03
 */
public interface IUmsUserService extends IService<UmsUser> {

    UmsUser getUmsUserByUserName(String username);

    Boolean register(UmsAppRegisterDTO umsAppRegisterDTO);

    LoginVO appLogin(UmsAppLoginDTO umsAppLoginDTO);

    void loginDataSetRedis(String token, UmsUser user);

    Boolean logout();
}
