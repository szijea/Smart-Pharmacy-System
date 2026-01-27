package com.pharmacy.service;

import com.pharmacy.dto.OrderRequest;
import com.pharmacy.dto.OrderResponse;
import com.pharmacy.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrderService {

    // 创建订单
    OrderResponse createOrder(OrderRequest orderRequest);

    // 根据ID获取订单 (主键为字符串)
    Optional<Order> getOrderById(String id);

    // 获取所有订单（分页）
    Page<Order> getAllOrders(Pageable pageable);

    // 根据订单号获取订单
    Optional<Order> getOrderByOrderId(String orderId);

    // 更新订单状态
    Order updateOrderStatus(String orderId, String status);

    // 删除订单
    boolean deleteOrder(String orderId);

    // 根据日期范围搜索订单
    List<Order> findOrdersByDateRange(LocalDate startDate, LocalDate endDate);

    // 根据会员ID获取订单
    List<Order> findOrdersByMemberId(String memberId);

    // 获取今日订单
    List<Order> getTodayOrders();

    // 获取销售额统计
    Double getTotalSalesByDateRange(LocalDate startDate, LocalDate endDate);

    // 获取订单数量统计
    Long getOrderCountByDateRange(LocalDate startDate, LocalDate endDate);

    // 新增：退单方法
    OrderResponse refundOrder(String orderId, String reason);

    // 新增：多条件搜索订单
    Page<Order> searchOrders(LocalDate startDate, LocalDate endDate, Integer status, Integer paymentType, String keyword, Pageable pageable);
}