package com.pharmacy.multitenant;

/**
 * 保存当前请求的店铺/租户ID，通过 ThreadLocal 实现每个请求独立。
 */
public final class TenantContext {
    private static final ThreadLocal<String> TENANT_HOLDER = new ThreadLocal<>();
    private TenantContext(){}
    public static void setTenant(String tenant){
        TENANT_HOLDER.set(tenant);
    }
    public static String getTenant(){
        return TENANT_HOLDER.get();
    }
    public static void clear(){
        TENANT_HOLDER.remove();
    }
    public static void setCurrentTenant(String tenant){
        setTenant(tenant);
    }
    public static String getCurrentTenant(){
        return getTenant();
    }
}
