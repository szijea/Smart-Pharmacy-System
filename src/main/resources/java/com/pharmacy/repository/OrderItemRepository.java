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

    // 添加缺失的方法
    @Query("SELECT oi.medicineId, SUM(oi.quantity), SUM(oi.subtotal) FROM OrderItem oi WHERE oi.orderId IN (SELECT o.orderId FROM Order o WHERE o.orderTime BETWEEN :start AND :end) GROUP BY oi.medicineId ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopProductsByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT oi.medicineId, SUM(oi.quantity), SUM(oi.subtotal) FROM OrderItem oi GROUP BY oi.medicineId ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopProducts();
}