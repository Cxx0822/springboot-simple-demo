package com.example.springboot_simple_demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    //配置跨域请求
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 允许所有接口都支持跨域
        registry.addMapping("/**")
                // 允许所有地址都可以访问
                .allowedOrigins("*")
                // 允许全部原始头信息
                .allowedHeaders("*")
                // 允许跨越发送cookie
                .allowCredentials(true)
                // 允许所有请求方法跨域调用
                .allowedMethods("*");
    }

    // 本地路径 要带上末尾的 "/"
    private static final String locationPath = "/**/";
    // 映射路径 例http://Ip:Port/downloads/***.***
    private static final String webPath = "/downloads/**";

    // 将本地文件映射到Url 可以直接下载
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(webPath).addResourceLocations("file:" + locationPath);
    }
}
