package com.wj.reggie.config;

import com.sun.xml.internal.ws.addressing.policy.AddressingFeatureConfigurator;
import com.wj.reggie.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

/**
 * @author wj
 * @version 1.0
 */
@Slf4j
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {
    /**
     * 静的リソースマッピングを設定する
     * @param registry
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("静的リソースマッピングを始める。。。");
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");

    }

    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {

        MappingJackson2HttpMessageConverter messageConverter=new MappingJackson2HttpMessageConverter();

        messageConverter.setObjectMapper(new JacksonObjectMapper());

        converters.add(0,messageConverter);

    }
}
