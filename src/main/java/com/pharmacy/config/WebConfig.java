package com.pharmacy.config;

import com.pharmacy.multitenant.TenantFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import com.pharmacy.config.BossAuthFilter;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {

    @Bean
    public FilterRegistrationBean<TenantFilter> tenantFilter() {
        FilterRegistrationBean<TenantFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new TenantFilter());
        registrationBean.addUrlPatterns("/api/*"); // 只对 API 请求应用此过滤器
        registrationBean.setOrder(1); // 在 ForwardedHeaderFilter 之后执行
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<BossAuthFilter> bossAuthFilter() {
        FilterRegistrationBean<BossAuthFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new BossAuthFilter());
        registrationBean.addUrlPatterns("/api/boss/*");
        registrationBean.setOrder(2);
        return registrationBean;
    }
}
