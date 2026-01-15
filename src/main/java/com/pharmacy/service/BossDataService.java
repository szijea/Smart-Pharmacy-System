package com.pharmacy.service;

import com.pharmacy.entity.Order;
import com.pharmacy.entity.Inventory;
import com.pharmacy.entity.Medicine;
import com.pharmacy.shared.entity.ProductTransferLog;
import com.pharmacy.shared.service.ProductTransferLogService;
import com.pharmacy.multitenant.MultiTenantDataSourceConfig;
import com.pharmacy.multitenant.TenantContext;
import com.pharmacy.repository.InventoryRepository;
import com.pharmacy.repository.OrderRepository;
import com.pharmacy.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private MedicineRepository medicineRepository;

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
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfYear = LocalDateTime.of(now.toLocalDate().with(TemporalAdjusters.firstDayOfYear()), LocalTime.MIN);
        LocalDateTime endOfYear = LocalDateTime.of(now.toLocalDate().with(TemporalAdjusters.lastDayOfYear()), LocalTime.MAX);

        for (String tenantId : tenantIds) {
            if ("default".equals(tenantId)) {
                continue; // Skip the default tenant
            }
            try {
                TenantContext.setCurrentTenant(tenantId);

                // Aggregate revenue from orders - Use Yearly sales to align with Tenant Stats
                Double yearlyParams = orderRepository.getTotalSalesByDateRange(startOfYear, endOfYear);
                if (yearlyParams != null) {
                    totalRevenue = totalRevenue.add(java.math.BigDecimal.valueOf(yearlyParams));
                }

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

    public Map<String, Object> getAllTenantStats() {
        Map<String, Object> allStats = new HashMap<>();
        Set<String> tenantIds = getAllTenantIds();

        for (String tenantId : tenantIds) {
            try {
                TenantContext.setCurrentTenant(tenantId);
                Map<String, Object> stats = new HashMap<>();
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime startOfDay = LocalDateTime.of(now.toLocalDate(), LocalTime.MIN);
                LocalDateTime endOfDay = LocalDateTime.of(now.toLocalDate(), LocalTime.MAX);

                LocalDateTime startOfMonth = LocalDateTime.of(now.toLocalDate().with(TemporalAdjusters.firstDayOfMonth()), LocalTime.MIN);
                LocalDateTime endOfMonth = LocalDateTime.of(now.toLocalDate().with(TemporalAdjusters.lastDayOfMonth()), LocalTime.MAX);

                LocalDateTime startOfYear = LocalDateTime.of(now.toLocalDate().with(TemporalAdjusters.firstDayOfYear()), LocalTime.MIN);
                LocalDateTime endOfYear = LocalDateTime.of(now.toLocalDate().with(TemporalAdjusters.lastDayOfYear()), LocalTime.MAX);

                Double daily = orderRepository.getTotalSalesByDateRange(startOfDay, endOfDay);
                Double monthly = orderRepository.getTotalSalesByDateRange(startOfMonth, endOfMonth);
                Double yearly = orderRepository.getTotalSalesByDateRange(startOfYear, endOfYear);

                // Get total stock count
                Long stockCount = inventoryRepository.findTotalStockQuantity();

                stats.put("dailySales", daily != null ? daily : 0.0);
                stats.put("monthlySales", monthly != null ? monthly : 0.0);
                stats.put("yearlySales", yearly != null ? yearly : 0.0);
                stats.put("stockCount", stockCount != null ? stockCount : 0);

                allStats.put(tenantId, stats);
            } catch (Exception e) {
                System.err.println("Error fetching stats for tenant " + tenantId + ": " + e.getMessage());
                allStats.put(tenantId, Map.of("error", e.getMessage()));
            } finally {
                TenantContext.clear();
            }
        }
        return allStats;
    }

    public Map<String, Object> getTenantInventory(String tenantId, int page, int size, String keyword) {
        Map<String, Object> result = new HashMap<>();
        try {
            TenantContext.setCurrentTenant(tenantId);
            Pageable pageable = PageRequest.of(page, size);
            Page<Inventory> inventoryPage;

            List<Map<String, Object>> displayItems = new ArrayList<>();
            long totalElements = 0;
            int totalPages = 0;

            if (keyword != null && !keyword.trim().isEmpty()) {
                 List<Medicine> meds = medicineRepository.searchByKeyword(keyword);
                 for (Medicine m : meds) {
                     List<Inventory> invs = inventoryRepository.findByMedicineId(m.getMedicineId());
                     for (Inventory inv : invs) {
                         // Limit to 100 results for search
                         if (displayItems.size() < 100) {
                             Map<String, Object> item = new HashMap<>();
                             inv.setMedicine(null); // Prevent LazyInitializationException
                             item.put("inventory", inv);
                             item.put("medicine", m);
                             displayItems.add(item);
                         }
                     }
                 }
                 totalElements = displayItems.size();
                 totalPages = 1;
            } else {
                inventoryPage = inventoryRepository.findAll(pageable);
                totalElements = inventoryPage.getTotalElements();
                totalPages = inventoryPage.getTotalPages();

                // Hydrate with medicine info
                for (Inventory inv : inventoryPage.getContent()) {
                    Map<String, Object> item = new HashMap<>();
                    inv.setMedicine(null); // Prevent LazyInitializationException
                    item.put("inventory", inv);
                    Optional<Medicine> med = medicineRepository.findById(inv.getMedicineId());
                    med.ifPresent(medicine -> item.put("medicine", medicine));
                    displayItems.add(item);
                }
            }

            result.put("content", displayItems);
            result.put("totalElements", totalElements);
            result.put("totalPages", totalPages);
            result.put("currentPage", page);

        } catch (Exception e) {
             result.put("error", e.getMessage());
        } finally {
            TenantContext.clear();
        }
        return result;
    }

    public java.util.Set<String> getAllTenantIds() {
        // Get all tenant IDs, but exclude 'default' as it's not a valid transfer target from the UI.
        java.util.Set<String> tenantIds = multiTenantDataSourceConfig.getTenantIds();
        return tenantIds.stream()
                .filter(id -> !"default".equals(id))
                .collect(java.util.stream.Collectors.toSet());
    }
}
