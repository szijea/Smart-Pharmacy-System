package com.pharmacy.service;

import com.pharmacy.entity.Order;
import com.pharmacy.shared.entity.ProductTransferLog;
import com.pharmacy.shared.service.ProductTransferLogService;
import com.pharmacy.multitenant.MultiTenantDataSourceConfig;
import com.pharmacy.multitenant.TenantContext;
import com.pharmacy.repository.InventoryRepository;
import com.pharmacy.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BossDataService {

    @Autowired
    private ProductTransferLogService productTransferLogService;

    @Autowired
    private MultiTenantDataSourceConfig multiTenantDataSourceConfig;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    /**
     * Fetches all product transfer logs.
     * This operation targets the 'default' database where logs are stored centrally.
     * @return A list of all product transfer logs.
     */
    public List<ProductTransferLog> getAllTransferLogs() {
        return productTransferLogService.getAllLogs();
    }

    public java.util.Map<String, Object> getAggregatedDashboardData() {
        java.util.Map<String, Object> dashboardData = new java.util.HashMap<>();
        java.math.BigDecimal totalRevenue = java.math.BigDecimal.ZERO;
        long totalInventoryCount = 0;

        // Get all tenant IDs, excluding 'default' as it's for system data.
        java.util.Set<String> tenantIds = multiTenantDataSourceConfig.getTenantIds();
        
        for (String tenantId : tenantIds) {
            if ("default".equals(tenantId)) {
                continue; // Skip the default tenant
            }
            try {
                TenantContext.setCurrentTenant(tenantId);

                // Aggregate revenue from orders
                totalRevenue = totalRevenue.add(orderRepository.findTotalRevenue() != null ? orderRepository.findTotalRevenue() : java.math.BigDecimal.ZERO);

                // Aggregate stock quantity from inventory
                totalInventoryCount += inventoryRepository.findTotalStockQuantity() != null ? inventoryRepository.findTotalStockQuantity() : 0;

            } finally {
                TenantContext.clear();
            }
        }

        dashboardData.put("totalRevenue", totalRevenue);
        dashboardData.put("totalInventoryCount", totalInventoryCount);
        dashboardData.put("tenantCount", tenantIds.stream().filter(id -> !"default".equals(id)).count());

        return dashboardData;
    }

    public java.util.Set<String> getAllTenantIds() {
        // Get all tenant IDs, but exclude 'default' as it's not a valid transfer target from the UI.
        java.util.Set<String> tenantIds = multiTenantDataSourceConfig.getTenantIds();
        return tenantIds.stream()
                .filter(id -> !"default".equals(id))
                .collect(java.util.stream.Collectors.toSet());
    }
}
