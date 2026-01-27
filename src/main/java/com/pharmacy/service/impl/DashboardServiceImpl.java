package com.pharmacy.service.impl;

import com.pharmacy.repository.OrderRepository;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.repository.InventoryRepository;
import com.pharmacy.service.DashboardService;
import com.pharmacy.service.StockAlertService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;
    private final MedicineRepository medicineRepository;
    private final InventoryRepository inventoryRepository;
    private final StockAlertService stockAlertService;

    public DashboardServiceImpl(OrderRepository orderRepository,
                                MedicineRepository medicineRepository,
                                InventoryRepository inventoryRepository,
                                StockAlertService stockAlertService) {
        this.orderRepository = orderRepository;
        this.medicineRepository = medicineRepository;
        this.inventoryRepository = inventoryRepository;
        this.stockAlertService = stockAlertService;
    }

    // 缓存控制台数据
    private Map<String, Object> dashboardCache = new HashMap<>();
    private LocalDateTime lastCacheUpdate;
    private static final long CACHE_DURATION_MINUTES = 5;

    @Override
    public Map<String, Object> getDashboardStats() {
        if (isCacheValid()) {
            return dashboardCache;
        }
        Map<String, Object> stats = new HashMap<>();
        try {
            Double todaySales = getTodaySales();
            // 使用区间查询获取昨日数据
            LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime todayEnd = todayStart.plusDays(1);
            LocalDateTime yStart = todayStart.minusDays(1);
            LocalDateTime yEnd = todayStart;
            Double yesterdaySales = orderRepository.getPaidSalesBetween(yStart, yEnd);
            double salesChange = yesterdaySales != null && yesterdaySales > 0 ? ((todaySales - yesterdaySales) / yesterdaySales) * 100 : 0.0;
            Integer todayOrders = getTodayOrders();
            Long yesterdayOrders = orderRepository.countPaidOrdersBetween(yStart, yEnd);
            double ordersChange = yesterdayOrders != null && yesterdayOrders > 0 ? ((todayOrders - yesterdayOrders) / (double) yesterdayOrders) * 100 : 0.0;
            Integer memberConsumption = getMemberConsumption();
            Integer yesterdayMembers = orderRepository.countDistinctMembersPaidBetween(yStart, yEnd);
            double memberChange = yesterdayMembers != null && yesterdayMembers > 0 ? ((memberConsumption - yesterdayMembers) / (double) yesterdayMembers) * 100 : 0.0;
            Integer lowStockCount = getLowStockCount();
            stats.put("todaySales", todaySales);
            stats.put("salesChange", Math.round(salesChange * 10) / 10.0);
            stats.put("todayOrders", todayOrders);
            stats.put("ordersChange", Math.round(ordersChange * 10) / 10.0);
            stats.put("memberConsumption", memberConsumption);
            stats.put("memberChange", Math.round(memberChange * 10) / 10.0);
            stats.put("lowStockCount", lowStockCount);
            stats.put("stockAlerts", lowStockCount);
            dashboardCache = stats;
            lastCacheUpdate = LocalDateTime.now();
        } catch (Exception e) {
            System.err.println("获取统计数据失败(返回空置0): " + e.getMessage());
            stats.put("todaySales", 0.0);
            stats.put("salesChange", 0.0);
            stats.put("todayOrders", 0);
            stats.put("ordersChange", 0.0);
            stats.put("memberConsumption", 0);
            stats.put("memberChange", 0.0);
            stats.put("lowStockCount", 0);
            stats.put("stockAlerts", 0);
        }
        return stats;
    }

    @Override
    public Map<String, Object> getSalesTrend(String period) {
        Map<String, Object> trendData = new HashMap<>();
        try {
            LocalDateTime now = LocalDateTime.now();
            switch (period) {
                case "day" -> {
                    LocalDateTime startDate = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
                    LocalDateTime endDate = startDate.plusDays(1);
                    // 直接拉取当天已支付订单，使用 Java 按小时聚合再映射 3 小时桶，避免 HOUR 函数方言差异
                    java.util.List<com.pharmacy.entity.Order> orders = orderRepository.findByOrderTimeBetween(startDate, endDate);
                    int[] buckets = new int[8];
                    for (com.pharmacy.entity.Order o : orders) {
                        if (o.getPaymentStatus() != null && o.getPaymentStatus() == 1 && o.getOrderTime() != null) {
                            int hour = o.getOrderTime().getHour();
                            int bucket = hour / 3; // 0..7
                            double amt = o.getTotalAmount();
                            buckets[bucket] += (int)Math.round(amt);
                        }
                    }
                    Integer[] hours = java.util.Arrays.stream(buckets).boxed().toArray(Integer[]::new);
                    trendData.put("labels", new String[]{"00:00", "03:00", "06:00", "09:00", "12:00", "15:00", "18:00", "21:00"});
                    trendData.put("data", hours);
                }
                case "month" -> {
                    LocalDateTime startDate = now.minusDays(29).withHour(0).withMinute(0).withSecond(0).withNano(0);
                    List<Object[]> salesData = orderRepository.getDailySales(startDate, now);
                    trendData.put("labels", new String[]{"第1周", "第2周", "第3周", "第4周"});
                    trendData.put("data", processMonthlySalesData(salesData));
                }
                default -> {
                    LocalDateTime startDate = now.minusDays(6).withHour(0).withMinute(0).withSecond(0).withNano(0);
                    List<Object[]> salesData = orderRepository.getDailySales(startDate, now);
                    trendData.put("labels", new String[]{"周一", "周二", "周三", "周四", "周五", "周六", "周日"});
                    trendData.put("data", processWeeklySalesData(salesData));
                }
            }
        } catch (Exception e) {
            System.err.println("获取销售趋势数据失败(返回空): " + e.getMessage());
            trendData.put("labels", new String[]{});
            trendData.put("data", new Integer[]{});
        }
        return trendData;
    }

    /**
     * 处理周销售数据
     */
    private Integer[] processWeeklySalesData(List<Object[]> salesData) {
        Integer[] weeklyData = new Integer[7];
        java.util.Arrays.fill(weeklyData, 0);
        if (salesData != null && !salesData.isEmpty()) {
            for (Object[] data : salesData) {
                if (data.length >= 2 && data[0] instanceof java.sql.Date) {
                    LocalDate localDate = ((java.sql.Date) data[0]).toLocalDate();
                    int index = localDate.getDayOfWeek().getValue() - 1;
                    if (index < 7) { // index >=0 总为真，简化条件
                        Double sales = (Double) data[1];
                        weeklyData[index] = sales != null ? sales.intValue() : 0;
                    }
                }
            }
        }
        return weeklyData;
    }

    /**
     * 处理月销售数据（按周分组）
     */
    private Integer[] processMonthlySalesData(List<Object[]> salesData) {
        Integer[] monthlyData = new Integer[4];
        java.util.Arrays.fill(monthlyData, 0);
        if (salesData != null && !salesData.isEmpty()) {
            for (Object[] data : salesData) {
                if (data.length >= 2 && data[0] instanceof java.sql.Date) {
                    LocalDate localDate = ((java.sql.Date) data[0]).toLocalDate();
                    int weekOfMonth = (localDate.getDayOfMonth() - 1) / 7;
                    if (weekOfMonth < 4) { // weekOfMonth>=0 恒真，省略
                        Double sales = (Double) data[1];
                        monthlyData[weekOfMonth] += sales != null ? sales.intValue() : 0;
                    }
                }
            }
        }
        return monthlyData;
    }

    @Override
    public Map<String, Object> getCategoryDistribution() {
        Map<String, Object> distribution = new HashMap<>();
        try {
            List<Object[]> categoryData = medicineRepository.getCategoryDistribution();
            List<String> labels = new ArrayList<>();
            List<Integer> data = new ArrayList<>();
            if (categoryData != null && !categoryData.isEmpty()) {
                for (Object[] item : categoryData) {
                    labels.add((String) item[0]);
                    data.add(((Long) item[1]).intValue());
                }
            }
            distribution.put("labels", labels.toArray(new String[0]));
            distribution.put("data", data.toArray(new Integer[0]));
            distribution.put("colors", new String[]{"#165DFF", "#36B37E", "#FFAB00", "#FF5630"});
        } catch (Exception e) {
            System.err.println("获取分类占比数据失败(返回空): " + e.getMessage());
            distribution.put("labels", new String[]{});
            distribution.put("data", new Integer[]{});
            distribution.put("colors", new String[]{"#165DFF", "#36B37E", "#FFAB00", "#FF5630"});
        }
        return distribution;
    }

    @Override
    public Map<String, Object> getStockAlerts() {
        // 使用真实的库存预警服务
        return stockAlertService.getDashboardStockAlerts();
    }

    @Override
    public List<Map<String, Object>> getTodayHotProducts() {
        List<Map<String, Object>> hotProducts = new ArrayList<>();
        try {
            LocalDateTime start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime end = start.plusDays(1);
            List<Object[]> hotProductData = orderRepository.getHotProductsBetween(start, end);
            if (hotProductData != null && !hotProductData.isEmpty()) {
                List<Object[]> stockData = inventoryRepository.getCurrentStockByMedicine();
                Map<String, Integer> stockMap = new HashMap<>();
                if (stockData != null) {
                    for (Object[] stock : stockData) {
                        stockMap.put((String) stock[0], ((Long) stock[1]).intValue());
                    }
                }
                for (Object[] data : hotProductData) {
                    Map<String, Object> product = new HashMap<>();
                    String medicineId = (String) data[0];
                    product.put("id", medicineId);
                    product.put("medicineId", medicineId);
                    product.put("medicineName", data[1]);
                    product.put("name", data[1]);
                    product.put("tradeName", data[2]);
                    product.put("specification", data[3]);
                    product.put("spec", data[3]);
                    product.put("unitPrice", data[4]);
                    product.put("price", data[4]);
                    product.put("todaySales", data[5]);
                    product.put("sales", data[5]);
                    product.put("todayAmount", data[6]);
                    product.put("amount", data[6]);
                    Integer currentStock = stockMap.get(medicineId);
                    boolean inInventory = currentStock != null;
                    currentStock = currentStock != null ? currentStock : 0;
                    product.put("currentStock", currentStock);
                    int safetyStock = inInventory ? 30 : 1;
                    product.put("safetyStock", safetyStock);
                    product.put("minStock", safetyStock);
                    product.put("inInventory", inInventory);
                    String stockStatus;
                    double ratio = currentStock * 1.0 / safetyStock;
                    if (currentStock == 0) stockStatus = "OUT";
                    else if (ratio <= 0.1) stockStatus = "CRITICAL";
                    else if (ratio <= 0.3) stockStatus = "LOW";
                    else if (ratio <= 0.8) stockStatus = "MEDIUM";
                    else stockStatus = "HIGH";
                    product.put("stockStatus", stockStatus);
                    hotProducts.add(product);
                }
            }
        } catch (Exception e) {
            System.err.println("获取热销药品数据异常(返回空列表): " + e.getMessage());
        }
        return hotProducts;
    }

    @Override
    public List<Map<String, Object>> getExpiringMedicines() {
        // 使用库存预警服务
        return stockAlertService.getExpiringMedicines();
    }

    @Override
    public Double getTodaySales() {
        try {
            LocalDateTime start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime end = start.plusDays(1);
            Double sales = orderRepository.getPaidSalesBetween(start, end);
            return sales != null ? sales : 0.0;
        } catch (Exception e) {
            System.err.println("获取今日销售额失败: " + e.getMessage());
            return 0.0;
        }
    }

    // 修复：返回 Integer 类型以匹配接口定义
    @Override
    public Integer getTodayOrders() {
        try {
            LocalDateTime start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime end = start.plusDays(1);
            Long orders = orderRepository.countPaidOrdersBetween(start, end);
            return orders != null ? orders.intValue() : 0;
        } catch (Exception e) {
            System.err.println("获取今日订单数失败: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public Integer getMemberConsumption() {
        try {
            LocalDateTime start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime end = start.plusDays(1);
            Integer members = orderRepository.countDistinctMembersPaidBetween(start, end);
            return members != null ? members : 0;
        } catch (Exception e) {
            System.err.println("获取会员消费人数失败: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public Integer getLowStockCount() {
        try {
            Integer count = inventoryRepository.getLowStockCount();
            return count != null ? count : 0;
        } catch (Exception e) {
            System.err.println("获取库存预警数量失败: " + e.getMessage());
            return 0; // 不再返回模拟
        }
    }

    @Override
    public Double getSalesChangePercent() {
        try {
            LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime todayEnd = todayStart.plusDays(1);
            LocalDateTime yStart = todayStart.minusDays(1);
            LocalDateTime yEnd = todayStart;
            Double todaySales = orderRepository.getPaidSalesBetween(todayStart, todayEnd);
            Double yesterdaySales = orderRepository.getPaidSalesBetween(yStart, yEnd);
            return yesterdaySales != null && yesterdaySales > 0 ? ((todaySales - yesterdaySales) / yesterdaySales) * 100 : 0.0;
        } catch (Exception e) { return 0.0; }
    }

    @Override
    public Double getOrdersChangePercent() {
        try {
            LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime todayEnd = todayStart.plusDays(1);
            LocalDateTime yStart = todayStart.minusDays(1);
            LocalDateTime yEnd = todayStart;
            Long todayOrders = orderRepository.countPaidOrdersBetween(todayStart, todayEnd);
            Long yesterdayOrders = orderRepository.countPaidOrdersBetween(yStart, yEnd);
            return yesterdayOrders != null && yesterdayOrders > 0 ? ((todayOrders - yesterdayOrders) / (double) yesterdayOrders) * 100 : 0.0;
        } catch (Exception e) { return 0.0; }
    }

    @Override
    public Double getMemberChangePercent() {
        try {
            LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime todayEnd = todayStart.plusDays(1);
            LocalDateTime yStart = todayStart.minusDays(1);
            LocalDateTime yEnd = todayStart;
            Integer todayMembers = orderRepository.countDistinctMembersPaidBetween(todayStart, todayEnd);
            Integer yesterdayMembers = orderRepository.countDistinctMembersPaidBetween(yStart, yEnd);
            return yesterdayMembers != null && yesterdayMembers > 0 ? ((todayMembers - yesterdayMembers) / (double) yesterdayMembers) * 100 : 0.0;
        } catch (Exception e) { return 0.0; }
    }

    @Override
    public void refreshDashboardCache() {
        dashboardCache.clear();
        lastCacheUpdate = null;
    }

    @Override
    public Map<String, Object> getExportData() {
        Map<String, Object> exportData = new HashMap<>();

        // 获取当前日期时间
        LocalDateTime now = LocalDateTime.now();
        String exportTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 基本统计信息
        exportData.put("exportTime", exportTime);
        exportData.put("reportTitle", "药房控制台数据报表");

        // 统计数据
        exportData.put("stats", getDashboardStats());

        // 销售趋势数据
        exportData.put("salesTrend", getSalesTrend("week"));

        // 分类占比数据
        exportData.put("categoryDistribution", getCategoryDistribution());

        // 库存预警数据
        exportData.put("stockAlerts", getStockAlerts());

        // 热销药品数据
        exportData.put("hotProducts", getTodayHotProducts());

        // 近效期药品
        exportData.put("expiringMedicines", getExpiringMedicines());

        return exportData;
    }

    /**
     * 检查缓存是否有效
     */
    private boolean isCacheValid() {
        if (lastCacheUpdate == null || dashboardCache.isEmpty()) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        return lastCacheUpdate.plusMinutes(CACHE_DURATION_MINUTES).isAfter(now);
    }
}
