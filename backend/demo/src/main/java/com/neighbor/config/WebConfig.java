package com.neighbor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.path}")
    private String uploadPath;
    
    @Value("${app.upload.url-prefix}")
    private String urlPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源访问
        registry.addResourceHandler(urlPrefix + "/**")
                .addResourceLocations("file:" + uploadPath + "/")
                .setCachePeriod(60 * 60 * 24);
    }
}
