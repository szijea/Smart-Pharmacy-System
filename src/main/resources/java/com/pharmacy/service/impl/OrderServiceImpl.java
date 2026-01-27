package com.pharmacy.service.impl;

import com.pharmacy.dto.OrderResponse;
import com.pharmacy.dto.OrderItemResponse;
import com.pharmacy.dto.OrderRequest;
import com.pharmacy.dto.OrderItemRequest;
import com.pharmacy.entity.Order;
import com.pharmacy.entity.OrderItem;
import com.pharmacy.entity.Medicine;
import com.pharmacy.repository.OrderRepository;
import com.pharmacy.repository.OrderItemRepository;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.service.OrderService;
import com.pharmacy.service.InventoryService; // 确保这行存在
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private InventoryService inventoryService; // 确保这行存在

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest) {
        System.out.println("=== 开始创建订单 ===");
        System.out.println("客户姓名: " + orderRequest.getCustomerName());
        System.out.println("商品数量: " + orderRequest.getItems().size());

        try {
            // 1. 检查库存是否充足
            for (OrderItemRequest item : orderRequest.getItems()) {
                boolean stockAvailable = inventoryService.checkStock(item.getProductId(), item.getQuantity());
                if (!stockAvailable) {
                    // 获取药品名称用于错误信息
                    String medicineName = medicineRepository.findById(item.getProductId())
                            .map(Medicine::getGenericName)
                            .orElse(item.getProductId());
                    throw new RuntimeException("药品 " + medicineName + " 库存不足，需求: " + item.getQuantity());
                }
            }

            // 2. 生成订单号
            String orderId = generateOrderId();
            System.out.println("生成的订单号: " + orderId);

            // 3. 计算订单总金额
            double totalAmount = orderRequest.getItems().stream()
                    .mapToDouble(item -> item.getUnitPrice().doubleValue() * item.getQuantity())
                    .sum();

            System.out.println("订单总金额: " + totalAmount);

            // 4. 创建订单实体
            Order order = new Order();
            order.setOrderId(orderId);
            order.setCashierId(1); // 默认收银员ID
            order.setCustomerName(orderRequest.getCustomerName());
            order.setTotalAmount(totalAmount);
            order.setDiscountAmount(0.0);
            order.setActualPayment(totalAmount);
            order.setPaymentType(1); // 默认现金支付
            order.setPaymentStatus(1); // 已支付
            order.setOrderTime(LocalDateTime.now());
            order.setPayTime(LocalDateTime.now());

            // 5. 保存订单
            Order savedOrder = orderRepository.save(order);
            System.out.println("订单保存成功，ID: " + savedOrder.getOrderId());

            // 6. 创建订单项并更新库存
            for (OrderItemRequest itemRequest : orderRequest.getItems()) {
                // 先保存订单项
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(savedOrder.getOrderId());
                orderItem.setMedicineId(itemRequest.getProductId());
                orderItem.setQuantity(itemRequest.getQuantity());
                orderItem.setUnitPrice(itemRequest.getUnitPrice().doubleValue());
                orderItem.setSubtotal(itemRequest.getUnitPrice().doubleValue() * itemRequest.getQuantity());

                orderItemRepository.save(orderItem);
                System.out.println("订单项保存成功: " + itemRequest.getProductId() + " x " + itemRequest.getQuantity());

                // 然后更新库存
                boolean stockUpdated = inventoryService.updateStockForOrder(
                        itemRequest.getProductId(),
                        itemRequest.getQuantity(),
                        savedOrder.getOrderId()
                );

                if (!stockUpdated) {
                    // 库存更新失败，抛出异常回滚事务
                    String medicineName = medicineRepository.findById(itemRequest.getProductId())
                            .map(Medicine::getGenericName)
                            .orElse(itemRequest.getProductId());
                    throw new RuntimeException("库存更新失败: " + medicineName);
                }
            }

            // 7. 构建响应
            OrderResponse response = convertToOrderResponse(savedOrder);
            System.out.println("✅ 订单创建完成: " + response.getOrderNumber());

            return response;

        } catch (Exception e) {
            System.err.println("❌ 创建订单失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("创建订单失败: " + e.getMessage());
        }
    }

    // 其他方法保持不变...
    @Override
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Override
    public Optional<Order> getOrderByOrderId(String orderId) {
        return orderRepository.findByOrderId(orderId);
    }

    @Override
    @Transactional
    public Order updateOrderStatus(String orderId, String status) {
        Optional<Order> orderOpt = orderRepository.findByOrderId(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            // 这里需要根据你的状态枚举来设置
            // order.setStatus(status);
            return orderRepository.save(order);
        }
        throw new RuntimeException("订单不存在: " + orderId);
    }

    @Override
    @Transactional
    public boolean deleteOrder(String orderId) {
        Optional<Order> orderOpt = orderRepository.findByOrderId(orderId);
        if (orderOpt.isPresent()) {
            // 先删除订单项
            orderItemRepository.deleteByOrderId(orderId);
            // 再删除订单
            orderRepository.deleteByOrderId(orderId);
            return true;
        }
        return false;
    }

    @Override
    public List<Order> findOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
        return orderRepository.findByOrderTimeBetween(startDateTime, endDateTime);
    }

    @Override
    public List<Order> findOrdersByMemberId(String memberId) {
        return orderRepository.findByMemberId(memberId);
    }

    @Override
    public List<Order> getTodayOrders() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay();
        return orderRepository.findByOrderTimeBetween(startOfDay, endOfDay);
    }

    @Override
    public Double getTotalSalesByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
        Double result = orderRepository.getTotalSalesByDateRange(startDateTime, endDateTime);
        return result != null ? result : 0.0;
    }

    @Override
    public Long getOrderCountByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
        Long result = orderRepository.getOrderCountByDateRange(startDateTime, endDateTime);
        return result != null ? result : 0L;
    }

    // === 私有辅助方法 ===

    private String generateOrderId() {
        // 生成格式如: O20241027123456 的订单号
        String timestamp = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "O" + timestamp;
    }

    private OrderResponse convertToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setOrderNumber(order.getOrderId());
        response.setCustomerName(order.getCustomerName());
        response.setTotalAmount(java.math.BigDecimal.valueOf(order.getTotalAmount()));
        response.setStatus("已完成");
        response.setCreateTime(order.getOrderTime());

        // 获取订单项
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getOrderId());
        List<OrderItemResponse> itemResponses = orderItems.stream()
                .map(this::convertToOrderItemResponse)
                .collect(Collectors.toList());

        response.setItems(itemResponses);
        return response;
    }

    private OrderItemResponse convertToOrderItemResponse(OrderItem orderItem) {
        OrderItemResponse response = new OrderItemResponse();
        response.setProductId(orderItem.getMedicineId());

        // 获取药品信息
        Optional<Medicine> medicineOpt = medicineRepository.findById(orderItem.getMedicineId());
        if (medicineOpt.isPresent()) {
            Medicine medicine = medicineOpt.get();
            response.setProductName(medicine.getGenericName());
        } else {
            response.setProductName("未知药品");
        }

        response.setQuantity(orderItem.getQuantity());
        response.setUnitPrice(java.math.BigDecimal.valueOf(orderItem.getUnitPrice()));
        response.setSubtotal(java.math.BigDecimal.valueOf(orderItem.getSubtotal()));

        return response;
    }
}