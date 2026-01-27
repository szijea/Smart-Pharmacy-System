package com.pharmacy.repository;

import com.pharmacy.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderId(String orderId);

    List<Order> findByOrderTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Order> findByMemberId(String memberId);

    void deleteByOrderId(String orderId);

    // 支付订单的销售额统计
    @Query("SELECT COALESCE(SUM(o.actualPayment), 0) FROM Order o WHERE o.orderTime BETWEEN :start AND :end AND o.paymentStatus = 1")
    Double getTotalSalesByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 支付订单的数量统计
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderTime BETWEEN :start AND :end AND o.paymentStatus = 1")
    Long getOrderCountByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 所有订单的总销售额（不考虑支付状态）
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderTime BETWEEN :start AND :end")
    Double getTotalSalesByTimeRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 所有订单的平均金额
    @Query("SELECT COALESCE(AVG(o.totalAmount), 0) FROM Order o WHERE o.orderTime BETWEEN :start AND :end")
    Double getAverageOrderValueByTimeRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 时间段内的订单数量
    Long countByOrderTimeBetween(LocalDateTime start, LocalDateTime end);

    // 每日销售额统计
    @Query("SELECT DATE(o.orderTime), COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderTime BETWEEN :start AND :end GROUP BY DATE(o.orderTime) ORDER BY DATE(o.orderTime)")
    List<Object[]> getDailySales(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 按支付状态统计订单数量
    @Query("SELECT CAST(o.paymentStatus AS string), COUNT(o) FROM Order o GROUP BY o.paymentStatus")
    List<Object[]> countOrdersByStatus();

    // 获取客户消费统计
    @Query("SELECT o.customerName, COUNT(o), COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderTime BETWEEN :start AND :end GROUP BY o.customerName ORDER BY SUM(o.totalAmount) DESC")
    List<Object[]> getCustomerSpendingStats(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}