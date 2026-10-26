package com.cloud.file.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * General Mapping Configuration
 * 
 * @author cloud
 */
@Configuration
public class ResourcesConfig implements WebMvcConfigurer
{
    /**
     * Root path for storing uploaded files locally
     */
    @Value("${file.path}")
    private String localFilePath;

    /**
     * Resource mapping path prefix
     */
    @Value("${file.prefix}")
    public String localFilePrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry)
    {
        /** Local file upload path */
        registry.addResourceHandler(localFilePrefix + "/**")
                .addResourceLocations("file:" + localFilePath + File.separator);
    }
    
    /**
     * Enable CORS
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Set allowed CORS routes
        registry.addMapping(localFilePrefix  + "/**")
                // Set allowed CORS request domains
                .allowedOrigins("*")
                // Set allowed methods
                .allowedMethods("GET");
    }
}