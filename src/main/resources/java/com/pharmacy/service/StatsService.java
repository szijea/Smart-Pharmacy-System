package com.pharmacy.service;

import java.time.LocalDateTime;
import java.util.Map;

public interface StatsService {

    // 获取销售统计
    Map<String, Object> getSalesStats(LocalDateTime startDate, LocalDateTime endDate);

    // 获取订单统计
    Map<String, Object> getOrderStats(LocalDateTime startDate, LocalDateTime endDate);

    // 获取商品排行
    Map<String, Object> getProductRanking(LocalDateTime startDate, LocalDateTime endDate, int limit);

    // 获取客户排行
    Map<String, Object> getCustomerRanking(LocalDateTime startDate, LocalDateTime endDate, int limit);
}