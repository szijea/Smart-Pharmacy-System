package com.pharmacy.service.impl;

import com.pharmacy.entity.Order;
import com.pharmacy.repository.OrderRepository;
import com.pharmacy.repository.OrderItemRepository;
import com.pharmacy.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsServiceImpl implements StatsService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Override
    public Map<String, Object> getSalesStats(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> stats = new HashMap<>();

        // 总销售额 - 修复类型转换
        Double totalSalesDouble = orderRepository.getTotalSalesByTimeRange(startDate, endDate);
        BigDecimal totalSales = totalSalesDouble != null ? BigDecimal.valueOf(totalSalesDouble) : BigDecimal.ZERO;
        stats.put("totalSales", totalSales);

        // 平均订单金额
        Double averageOrderValueDouble = orderRepository.getAverageOrderValueByTimeRange(startDate, endDate);
        BigDecimal averageOrderValue = averageOrderValueDouble != null ? BigDecimal.valueOf(averageOrderValueDouble) : BigDecimal.ZERO;
        stats.put("averageOrderValue", averageOrderValue);

        // 订单数量
        Long orderCount = orderRepository.countByOrderTimeBetween(startDate, endDate);
        stats.put("orderCount", orderCount);

        // 每日销售额
        List<Object[]> dailySales = orderRepository.getDailySales(startDate, endDate);
        List<Map<String, Object>> dailySalesList = new ArrayList<>();
        for (Object[] dailySale : dailySales) {
            Map<String, Object> dailyData = new HashMap<>();
            dailyData.put("date", dailySale[0]);
            // 确保销售额也是BigDecimal
            Object salesValue = dailySale[1];
            BigDecimal dailySalesBigDecimal = salesValue instanceof Double ?
                    BigDecimal.valueOf((Double) salesValue) :
                    (salesValue instanceof BigDecimal ? (BigDecimal) salesValue : BigDecimal.ZERO);
            dailyData.put("sales", dailySalesBigDecimal);
            dailySalesList.add(dailyData);
        }
        stats.put("dailySales", dailySalesList);

        return stats;
    }

    @Override
    public Map<String, Object> getOrderStats(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> stats = new HashMap<>();

        // 订单状态分布
        List<Object[]> statusCounts = orderRepository.countOrdersByStatus();
        Map<String, Long> statusDistribution = new HashMap<>();
        for (Object[] statusCount : statusCounts) {
            statusDistribution.put((String) statusCount[0], (Long) statusCount[1]);
        }
        stats.put("statusDistribution", statusDistribution);

        // 时间段内的订单列表 - 修复方法调用
        List<Map<String, Object>> ordersInPeriod = new ArrayList<>();
        // 使用现有的 findByOrderTimeBetween 方法
        orderRepository.findByOrderTimeBetween(startDate, endDate).forEach(order -> {
            Map<String, Object> orderInfo = new HashMap<>();
            orderInfo.put("orderNumber", order.getOrderId());  // 改为 getOrderId()
            orderInfo.put("customerName", order.getCustomerName());
            orderInfo.put("totalAmount", BigDecimal.valueOf(order.getTotalAmount()));  // 转换为BigDecimal
            orderInfo.put("status", "已完成");  // 根据实际情况设置状态
            orderInfo.put("createTime", order.getOrderTime());  // 改为 getOrderTime()
            ordersInPeriod.add(orderInfo);
        });
        stats.put("ordersInPeriod", ordersInPeriod);

        return stats;
    }

    @Override
    public Map<String, Object> getProductRanking(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        Map<String, Object> ranking = new HashMap<>();

        // 使用 OrderItemRepository 获取商品排行
        List<Object[]> topProducts = orderItemRepository.findTopProductsByDateRange(startDate, endDate);
        List<Map<String, Object>> productRanking = new ArrayList<>();

        int count = 0;
        for (Object[] product : topProducts) {
            if (count >= limit) break;

            Map<String, Object> productData = new HashMap<>();
            productData.put("productId", product[0]);
            productData.put("productName", product[1]);
            productData.put("totalQuantity", product[2]);
            // 确保收入是BigDecimal
            Object revenueValue = product[3];
            BigDecimal revenueBigDecimal = revenueValue instanceof Double ?
                    BigDecimal.valueOf((Double) revenueValue) :
                    (revenueValue instanceof BigDecimal ? (BigDecimal) revenueValue : BigDecimal.ZERO);
            productData.put("totalRevenue", revenueBigDecimal);
            productRanking.add(productData);
            count++;
        }

        ranking.put("topProducts", productRanking);
        ranking.put("timeRange", Map.of("start", startDate, "end", endDate));

        return ranking;
    }

    @Override
    public Map<String, Object> getCustomerRanking(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        Map<String, Object> ranking = new HashMap<>();

        // 修复：使用现有的方法或者创建新的查询
        List<Object[]> customerStats = orderRepository.getCustomerSpendingStats(startDate, endDate);
        List<Map<String, Object>> customerRanking = new ArrayList<>();

        int count = 0;
        for (Object[] customer : customerStats) {
            if (count >= limit) break;

            Map<String, Object> customerData = new HashMap<>();
            customerData.put("customerName", customer[0]);
            customerData.put("orderCount", customer[1]);
            // 确保消费金额是BigDecimal
            Object spendingValue = customer[2];
            BigDecimal spendingBigDecimal = spendingValue instanceof Double ?
                    BigDecimal.valueOf((Double) spendingValue) :
                    (spendingValue instanceof BigDecimal ? (BigDecimal) spendingValue : BigDecimal.ZERO);
            customerData.put("totalSpending", spendingBigDecimal);
            customerRanking.add(customerData);
            count++;
        }

        ranking.put("topCustomers", customerRanking);
        ranking.put("timeRange", Map.of("start", startDate, "end", endDate));

        return ranking;
    }

    // 新增方法：获取所有商品排行（不按时间范围）
    public Map<String, Object> getAllProductRanking(int limit) {
        Map<String, Object> ranking = new HashMap<>();

        List<Object[]> topProducts = orderItemRepository.findTopProducts();
        List<Map<String, Object>> productRanking = new ArrayList<>();

        int count = 0;
        for (Object[] product : topProducts) {
            if (count >= limit) break;

            Map<String, Object> productData = new HashMap<>();
            productData.put("productId", product[0]);
            productData.put("productName", product[1]);
            productData.put("totalQuantity", product[2]);
            // 确保收入是BigDecimal
            Object revenueValue = product[3];
            BigDecimal revenueBigDecimal = revenueValue instanceof Double ?
                    BigDecimal.valueOf((Double) revenueValue) :
                    (revenueValue instanceof BigDecimal ? (BigDecimal) revenueValue : BigDecimal.ZERO);
            productData.put("totalRevenue", revenueBigDecimal);
            productRanking.add(productData);
            count++;
        }

        ranking.put("topProducts", productRanking);
        return ranking;
    }
}