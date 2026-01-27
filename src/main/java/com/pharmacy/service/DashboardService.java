package com.pharmacy.service;

import java.util.Map;
import java.util.List;

public interface DashboardService {

    /**
     * 获取控制台统计数据
     */
    Map<String, Object> getDashboardStats();

    /**
     * 获取销售趋势数据
     * @param period 周期：day, week, month
     */
    Map<String, Object> getSalesTrend(String period);

    /**
     * 获取药品分类占比数据
     */
    Map<String, Object> getCategoryDistribution();

    /**
     * 获取库存预警数据（包含近效期、低库存、缺货）
     */
    Map<String, Object> getStockAlerts();

    /**
     * 获取今日热销药品
     */
    List<Map<String, Object>> getTodayHotProducts();

    /**
     * 获取近效期药品
     */
    List<Map<String, Object>> getExpiringMedicines();

    /**
     * 获取今日销售额
     */
    Double getTodaySales();

    /**
     * 获取今日订单数
     */
    Integer getTodayOrders();

    /**
     * 获取会员消费人数
     */
    Integer getMemberConsumption();

    /**
     * 获取库存预警数量
     */
    Integer getLowStockCount();

    /**
     * 获取销售数据变化百分比（较昨日）
     */
    Double getSalesChangePercent();

    /**
     * 获取订单数据变化百分比（较昨日）
     */
    Double getOrdersChangePercent();

    /**
     * 获取会员数据变化百分比（较昨日）
     */
    Double getMemberChangePercent();

    /**
     * 刷新控制台数据缓存
     */
    void refreshDashboardCache();

    /**
     * 获取导出报表数据
     */
    Map<String, Object> getExportData();


}