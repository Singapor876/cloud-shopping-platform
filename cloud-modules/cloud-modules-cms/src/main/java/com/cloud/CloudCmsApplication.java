package com.cloud;

import com.cloud.common.security.annotation.EnableCustomConfig;
import com.cloud.common.security.annotation.EnableRyFeignClients;
import com.cloud.common.swagger.annotation.EnableCustomSwagger2;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 酒店服务
 *
 * 酒店管理，评论
 * 
 * @author cloud
 */
@EnableAsync
@EnableCustomConfig
@EnableRyFeignClients
@MapperScan("com.cloud.**.mapper")
@EnableCustomSwagger2
@SpringBootApplication
public class CloudCmsApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(CloudCmsApplication.class, args);
        System.out.println("商品服务模块启动成功");
    }
}
