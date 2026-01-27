package com.pharmacy.multitenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 每个 HTTP 请求开始时解析店铺标识写入 TenantContext，完成后清除。
 * 解析顺序：Header X-Shop-Id > 请求参数 shopId > 默认 default。
 */

public class TenantFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String shopId = resolveShopId(request);
            if(shopId != null) {
                TenantContext.setTenant(shopId);
            }
            filterChain.doFilter(request,response);
        } finally {
            TenantContext.clear();
        }
    }

    private String resolveShopId(HttpServletRequest request){
        String header = request.getHeader("X-Shop-Id");
        if(header != null && !header.isBlank()) return header.trim();
        String param = request.getParameter("shopId");
        if(param != null && !param.isBlank()) return param.trim();
        // 如果是 boss 登录接口，也可以不特殊处理，因为 BossService 会强制切换
        // 但为了日志清晰，我们可以返回 null, 表示无租户限制 (RoutingDataSource should handle default)
        return null;
    }
}
