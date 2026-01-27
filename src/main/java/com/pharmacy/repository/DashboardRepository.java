// DashboardRepository.java
package com.pharmacy.repository;

import com.pharmacy.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
public interface DashboardRepository extends JpaRepository<Medicine, String> {

    // 库存预警查询
    @Query(value = """
        SELECT 
            m.medicine_id as medicineId,
            m.medicine_name as medicineName,
            m.specification,
            m.category,
            COALESCE(s.quantity, 0) as currentStock,
            m.min_stock as minStock,
            m.max_stock as maxStock,
            m.unit_price as unitPrice,
            CASE 
                WHEN COALESCE(s.quantity, 0) <= m.min_stock THEN 'CRITICAL'
                WHEN COALESCE(s.quantity, 0) <= m.min_stock * 1.5 THEN 'LOW'
                ELSE 'NORMAL'
            END as alertLevel,
            DATEDIFF(m.expiry_date, CURDATE()) as daysToExpiry,
            m.expiry_date as expiryDate
        FROM medicine m
        LEFT JOIN stock s ON m.medicine_id = s.medicine_id
        WHERE COALESCE(s.quantity, 0) <= m.min_stock * 1.5
           OR DATEDIFF(m.expiry_date, CURDATE()) <= 30
        ORDER BY alertLevel, daysToExpiry ASC
        """, nativeQuery = true)
    List<Map<String, Object>> findStockAlerts();

    // 今日热销药品
    @Query(value = """
        SELECT 
            m.medicine_id as medicineId,
            m.medicine_name as medicineName,
            m.specification,
            m.category,
            m.unit_price as unitPrice,
            COUNT(oi.id) as todaySales,
            SUM(oi.quantity * oi.unit_price) as todayAmount
        FROM medicine m
        INNER JOIN order_item oi ON m.medicine_id = oi.medicine_id
        INNER JOIN `order` o ON oi.order_id = o.order_id
        WHERE DATE(o.order_date) = CURDATE()
        GROUP BY m.medicine_id, m.medicine_name, m.specification, m.category, m.unit_price
        ORDER BY todaySales DESC
        LIMIT 10
        """, nativeQuery = true)
    List<Map<String, Object>> findTodayHotProducts();

    // 近效期药品
    @Query(value = """
        SELECT 
            m.medicine_id as medicineId,
            m.medicine_name as medicineName,
            m.specification,
            m.expiry_date as expiryDate,
            COALESCE(s.quantity, 0) as remainingStock
        FROM medicine m
        LEFT JOIN stock s ON m.medicine_id = s.medicine_id
        WHERE m.expiry_date IS NOT NULL 
          AND DATEDIFF(m.expiry_date, CURDATE()) BETWEEN 1 AND 90
        ORDER BY m.expiry_date ASC
        LIMIT 20
        """, nativeQuery = true)
    List<Map<String, Object>> findExpiringMedicines();

    // 今日销售额
    @Query(value = "SELECT COALESCE(SUM(total_amount), 0) FROM `order` WHERE DATE(order_date) = CURDATE()", nativeQuery = true)
    BigDecimal getTodaySales();

    // 今日订单数
    @Query(value = "SELECT COUNT(*) FROM `order` WHERE DATE(order_date) = CURDATE()", nativeQuery = true)
    Integer getTodayOrders();

    // 今日会员消费人数
    @Query(value = "SELECT COUNT(DISTINCT member_id) FROM `order` WHERE DATE(order_date) = CURDATE() AND member_id IS NOT NULL", nativeQuery = true)
    Integer getTodayMemberConsumption();

    // 库存预警数量
    @Query(value = """
        SELECT COUNT(*) FROM medicine m 
        LEFT JOIN stock s ON m.medicine_id = s.medicine_id 
        WHERE COALESCE(s.quantity, 0) <= m.min_stock * 1.5
        """, nativeQuery = true)
    Integer getLowStockCount();
}