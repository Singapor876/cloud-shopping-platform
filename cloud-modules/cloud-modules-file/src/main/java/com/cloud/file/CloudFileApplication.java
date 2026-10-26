package com.cloud.file;

import com.cloud.common.swagger.annotation.EnableCustomSwagger2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * 文件服务
 * 
 * @author cloud
 */
@EnableCustomSwagger2
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class CloudFileApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(CloudFileApplication.class, args);
        System.out.println("文件服务模块启动成功");
    }


}
