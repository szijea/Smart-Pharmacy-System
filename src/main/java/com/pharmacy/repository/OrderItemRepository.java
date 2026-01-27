package com.pharmacy.repository;

import com.pharmacy.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(String orderId);

    void deleteByOrderId(String orderId);

    // 今日/时间范围热销：只统计已支付订单（paymentStatus = 1）
    // 修复：使用原生 SQL 并对 order 表名加反引号，避免关键字冲突
    @Query(value = "SELECT oi.medicine_id, SUM(oi.quantity), SUM(oi.subtotal) " +
            "FROM order_item oi " +
            "JOIN `order` o ON oi.order_id = o.order_id " +
            "WHERE o.order_time BETWEEN :start AND :end " +
            "  AND o.payment_status = 1 " +
            "GROUP BY oi.medicine_id " +
            "ORDER BY SUM(oi.quantity) DESC", nativeQuery = true)
    List<Object[]> findTopProductsByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT oi.medicineId, SUM(oi.quantity), SUM(oi.subtotal) FROM OrderItem oi GROUP BY oi.medicineId ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopProducts();

    long countByMedicineId(String medicineId);
}