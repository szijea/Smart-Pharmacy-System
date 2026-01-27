package com.pharmacy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

@Configuration
public class ProxySupportConfig {

    // 支持 Nginx 反向代理的 X-Forwarded-* 头解析，确保生成的 redirect/url 使用外部域名与协议
    @Bean
    public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter(){
        FilterRegistrationBean<ForwardedHeaderFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new ForwardedHeaderFilter());
        bean.setOrder(0); // 提前执行，保证后续安全/路由过滤器拿到正确的请求URL
        return bean;
    }
}

