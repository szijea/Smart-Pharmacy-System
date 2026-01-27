package com.pharmacy.multitenant;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 根据当前线程保存的租户ID返回对应数据源 key。
 */
public class StoreRoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        String tenant = TenantContext.getTenant();
        return tenant == null || tenant.isBlank() ? "default" : tenant;
    }
}

