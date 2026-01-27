// StockAlertService.java
package com.pharmacy.service;

import com.pharmacy.entity.StockAlert;
import java.util.List;
import java.util.Map;

public interface StockAlertService {

    // 获取所有库存预警
    List<StockAlert> getAllAlerts();

    // 获取未处理的预警
    List<StockAlert> getUnhandledAlerts();

    // 根据类型获取预警
    List<StockAlert> getAlertsByType(Integer alertType);

    // 处理预警
    boolean handleAlert(Long alertId);

    // 获取控制台需要的库存预警数据（近效期、低库存、缺货）
    Map<String, Object> getDashboardStockAlerts();

    // 检查并生成库存预警
    void checkAndGenerateAlerts();

    // 获取近效期药品（60天内到期）
    List<Map<String, Object>> getExpiringMedicines();

    // 获取低库存药品（库存低于安全库存）
    List<Map<String, Object>> getLowStockMedicines();

    // 获取缺货药品
    List<Map<String, Object>> getOutOfStockMedicines();
}