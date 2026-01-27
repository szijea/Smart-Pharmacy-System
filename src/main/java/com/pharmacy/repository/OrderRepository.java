package com.pharmacy.repository;

import com.pharmacy.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface OrderRepository extends JpaRepository<Order, String>, JpaSpecificationExecutor<Order> { // ID 类型改为 String 以匹配 Order.orderId

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

    // 在 OrderRepository.java 中添加这些方法

    // 替换原 getTodaySales/getYesterdaySales 等有问题的 DATE() 查询，改为时间范围参数
    @Query("SELECT COALESCE(SUM(o.actualPayment), 0) FROM Order o WHERE o.paymentStatus = 1 AND o.orderTime >= :start AND o.orderTime < :end")
    Double getPaidSalesBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.paymentStatus = 1 AND o.orderTime >= :start AND o.orderTime < :end")
    Long countPaidOrdersBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(DISTINCT o.memberId) FROM Order o WHERE o.paymentStatus = 1 AND o.memberId IS NOT NULL AND o.orderTime >= :start AND o.orderTime < :end")
    Integer countDistinctMembersPaidBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.memberId = :memberId AND o.paymentStatus = 1")
    Long countPaidOrdersByMember(@Param("memberId") String memberId);

    @Query("SELECT MAX(o.orderTime) FROM Order o WHERE o.memberId = :memberId AND o.paymentStatus = 1")
    LocalDateTime findLastPaidOrderTime(@Param("memberId") String memberId);

    @Query("SELECT o.memberId, COUNT(o), MAX(o.orderTime) FROM Order o WHERE o.paymentStatus=1 AND o.memberId IN :memberIds GROUP BY o.memberId")
    List<Object[]> aggregateMemberConsumption(@Param("memberIds") Collection<String> memberIds);

    // 获取今日活跃会员（已支付订单的会员ID去重）
    @Query(value = "SELECT DISTINCT member_id FROM `order` WHERE payment_status=1 AND member_id IS NOT NULL AND DATE(order_time)=CURDATE()", nativeQuery = true)
    List<String> getTodayActiveMembers();

    // 新增：当日按小时销售额（已支付），返回 hour(0-23), sum(totalAmount)
    @Query("SELECT FUNCTION('HOUR', o.orderTime) as hr, COALESCE(SUM(o.totalAmount),0) FROM Order o WHERE o.orderTime BETWEEN :start AND :end AND o.paymentStatus = 1 GROUP BY FUNCTION('HOUR', o.orderTime) ORDER BY hr")
    List<Object[]> getHourlySales(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT m.medicine_id, m.generic_name, m.trade_name, m.spec, m.retail_price, " +
            "SUM(oi.quantity) as total_sold, SUM(oi.quantity * oi.unit_price) as total_amount " +
            "FROM order_item oi " +
            "JOIN medicine m ON oi.medicine_id = m.medicine_id " +
            "JOIN `order` o ON oi.order_id = o.order_id " +
            "WHERE o.payment_status = 1 AND o.order_time >= :start AND o.order_time < :end " +
            "GROUP BY m.medicine_id, m.generic_name, m.trade_name, m.spec, m.retail_price " +
            "ORDER BY total_sold DESC LIMIT 10", nativeQuery = true)
    List<Object[]> getHotProductsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.paymentStatus = :status AND o.orderTime >= :start AND o.orderTime < :end")
    Long countByPaymentStatusAndOrderTimeBetween(@Param("status") int status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 新增退款金额统计方法：在时间范围内，已退款订单的实付金额汇总，用于将今日销售额扣除退款。
    @Query("SELECT COALESCE(SUM(o.actualPayment), 0) FROM Order o WHERE o.paymentStatus = 2 AND o.refundTime >= :start AND o.refundTime < :end")
    Double getRefundedAmountBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(o.actualPayment) FROM Order o WHERE o.paymentStatus = 1")
    BigDecimal findTotalRevenue();
}
