package com.pharmacy.service.impl;

import com.pharmacy.dto.OrderResponse;
import com.pharmacy.dto.OrderItemResponse;
import com.pharmacy.dto.OrderRequest;
import com.pharmacy.dto.OrderItemRequest;
import com.pharmacy.entity.Order;
import com.pharmacy.entity.OrderItem;
import com.pharmacy.entity.Medicine;
import com.pharmacy.entity.Employee;
import com.pharmacy.repository.OrderRepository;
import com.pharmacy.repository.OrderItemRepository;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.repository.EmployeeRepository;
import com.pharmacy.service.OrderService;
import com.pharmacy.service.InventoryService;
import com.pharmacy.repository.MemberRepository;
import com.pharmacy.entity.Member;
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
    private InventoryService inventoryService;

    @Autowired(required = false)
    private MemberConsumptionUpdater memberConsumptionUpdater; // 可选组件，不存在时日志提示

    @Autowired
    private EmployeeRepository employeeRepository; // 新增: 动态获取收银员
    @Autowired
    private MemberRepository memberRepository; // 新增: 校验会员是否存在

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest) {
        System.out.println("=== 开始创建订单 ===");
        System.out.println("客户姓名: " + orderRequest.getCustomerName());
        System.out.println("会员ID: " + orderRequest.getMemberId());
        System.out.println("商品数量: " + orderRequest.getItems().size());

        try {
            // 1. 检查库存是否充足
            for (OrderItemRequest item : orderRequest.getItems()) {
                boolean stockAvailable = inventoryService.checkStock(item.getProductId(), item.getQuantity());
                if (!stockAvailable) {
                    String medicineName = medicineRepository.findById(item.getProductId())
                            .map(Medicine::getGenericName)
                            .orElse(String.valueOf(item.getProductId()));
                    throw new RuntimeException("药品 " + medicineName + " 库存不足，需求: " + item.getQuantity());
                }
            }

            // 2. 生成订单号
            String orderId = generateOrderId();
            System.out.println("生成的订单号: " + orderId);

            // 3. 计算订单总金额 - 使用前端传递的金额信息
            double totalAmount = orderRequest.getTotalAmount() != null ?
                    orderRequest.getTotalAmount().doubleValue() :
                    orderRequest.getItems().stream()
                            .mapToDouble(item -> item.getUnitPrice().doubleValue() * item.getQuantity())
                            .sum();

            // 使用前端传递的折扣信息
            double discountAmount = orderRequest.getDiscountAmount() != null ?
                    orderRequest.getDiscountAmount().doubleValue() : 0.0;
            double originalAmount = orderRequest.getOriginalAmount() != null ?
                    orderRequest.getOriginalAmount().doubleValue() : totalAmount;

            System.out.println("订单总金额: " + totalAmount);
            System.out.println("折扣金额: " + discountAmount);
            System.out.println("原始金额: " + originalAmount);

            // 4. 创建订单实体
            Order order = new Order();
            order.setOrderId(orderId);
            // 动态解析收银员ID，避免外键失败
            Integer cashierId = resolveCashierId();
            if (cashierId == null) {
                throw new RuntimeException("当前租户没有可用收银员账号，无法创建订单");
            }
            order.setCashierId(cashierId);

            // 设置会员ID（需要存在性校验）
            if (orderRequest.getMemberId() != null && !orderRequest.getMemberId().trim().isEmpty()) {
                String rawMemberId = orderRequest.getMemberId().trim();
                boolean memberExists = memberRepository.findById(rawMemberId).isPresent();
                if (memberExists) {
                    order.setMemberId(rawMemberId);
                    System.out.println("设置会员ID: " + rawMemberId);
                } else {
                    System.err.println("[Order] 当前租户不存在会员ID=" + rawMemberId + "，跳过关联，避免外键错误");
                    order.setMemberId(null); // 避免 1452 外键错误
                }
            } else {
                order.setMemberId(null);
                System.out.println("无会员信息");
            }

            order.setCustomerName(orderRequest.getCustomerName());
            order.setTotalAmount(originalAmount);
            order.setDiscountAmount(discountAmount);
            order.setActualPayment(totalAmount);

            // 支付方式转换
            Integer paymentType = convertPaymentMethod(orderRequest.getPaymentMethod());
            order.setPaymentType(paymentType);
            order.setPaymentStatus(1); // 已支付
            order.setOrderTime(LocalDateTime.now());
            order.setPayTime(LocalDateTime.now());

            // 5. 保存订单
            Order savedOrder = orderRepository.save(order);
            System.out.println("订单保存成功，ID: " + savedOrder.getOrderId());
            System.out.println("订单会员ID: " + savedOrder.getMemberId());

            // 5.1 订单积分计算并累计到会员（按实付金额四舍五入为积分）
            try {
                double pay = savedOrder.getActualPayment() != null ? savedOrder.getActualPayment() : 0.0;
                int createdPoints = (int) Math.round(pay);
                if (createdPoints < 0) createdPoints = 0;
                // 回写订单的获得积分
                try {
                    savedOrder.setCreatedPoints(createdPoints);
                    savedOrder = orderRepository.save(savedOrder);
                } catch (Exception e){ System.err.println("写入订单积分失败:"+e.getMessage()); }
                // 累加到会员
                String mid = savedOrder.getMemberId();
                if (mid != null && !mid.isBlank() && createdPoints > 0) {
                    try {
                        Optional<Member> memOpt = memberRepository.findById(mid);
                        if (memOpt.isPresent()) {
                            Member mem = memOpt.get();
                            mem.addPoints(createdPoints);
                            memberRepository.save(mem);
                            System.out.println("会员积分累计成功: memberId="+mid+" +"+createdPoints);
                        } else {
                            System.err.println("会员不存在，跳过积分累计: memberId="+mid);
                        }
                    } catch (Exception e){ System.err.println("会员积分累计失败:"+e.getMessage()); }
                }
            } catch (Exception e){ System.err.println("订单积分处理异常:"+e.getMessage()); }

            // 6. 创建订单项并更新库存
            for (OrderItemRequest itemRequest : orderRequest.getItems()) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(savedOrder.getOrderId());
                // 确保商品ID为 String
                String productId = String.valueOf(itemRequest.getProductId());
                orderItem.setMedicineId(productId);
                orderItem.setQuantity(itemRequest.getQuantity());
                orderItem.setUnitPrice(itemRequest.getUnitPrice().doubleValue());
                orderItem.setSubtotal(itemRequest.getUnitPrice().doubleValue() * itemRequest.getQuantity());

                orderItemRepository.save(orderItem);
                System.out.println("订单项保存成功: " + productId + " x " + itemRequest.getQuantity());

                // 更新库存（使用 String 类型 medicineId）
                boolean stockUpdated = inventoryService.updateStockForOrder(
                        productId,
                        itemRequest.getQuantity(),
                        savedOrder.getOrderId()
                );

                if (!stockUpdated) {
                    String medicineName = medicineRepository.findById(productId)
                            .map(Medicine::getGenericName)
                            .orElse(productId);
                    throw new RuntimeException("库存更新失败: " + medicineName);
                }
            }

            // 7. 构建响应
            OrderResponse response = convertToOrderResponse(savedOrder);
            // 新增：订单创建后异步刷新会员消费统计
            triggerMemberStatsUpdate(savedOrder.getMemberId());
            System.out.println("✅ 订单创建完成并触发会员消费统计刷新: " + response.getOrderNumber());
            return response;

        } catch (Exception e) {
            System.err.println("❌ 创建订单失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("创建订单失败: " + e.getMessage());
        }
    }

    // 支付方式转换方法
    private Integer convertPaymentMethod(String paymentMethod) {
        if (paymentMethod == null) return 1; // 默认现金

        switch (paymentMethod.toLowerCase()) {
            case "cash": return 1;
            case "wechat": return 2;
            case "alipay": return 3;
            case "insurance": return 4;
            case "card": return 5;
            default: return 1;
        }
    }

    @Override
    public Optional<Order> getOrderById(String id) { // 保持 String
        return orderRepository.findById(id);
    }

    @Override
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Override
    public Optional<Order> getOrderByOrderId(String orderId) { // 改为 String
        return orderRepository.findByOrderId(orderId);
    }

    @Override
    @Transactional
    public Order updateOrderStatus(String orderId, String status) {
        Optional<Order> orderOpt = orderRepository.findByOrderId(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            // 这里需要根据你的状态枚举来设置
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

    @Override
    @Transactional
    public OrderResponse refundOrder(String orderId, String reason) {
        Optional<Order> opt = orderRepository.findByOrderId(orderId);
        if(opt.isEmpty()) throw new RuntimeException("订单不存在: " + orderId);
        Order order = opt.get();
        if(order.getPaymentStatus()!=null && order.getPaymentStatus()==2){
            throw new RuntimeException("订单已退款");
        }
        if(order.getPaymentStatus()==null || order.getPaymentStatus()==0){
            throw new RuntimeException("未支付订单不可退款");
        }
        // 恢复库存(根据订单项)
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        for(OrderItem it: items){
            try { inventoryService.restoreStock(it.getMedicineId(), it.getQuantity(), orderId); } catch (Exception e){ System.err.println("恢复库存失败:"+e.getMessage()); }
        }
        order.setPaymentStatus(2); // 已退款
        order.setRefundTime(LocalDateTime.now());
        OrderResponse resp = convertToOrderResponse(orderRepository.save(order));
        triggerMemberStatsUpdate(order.getMemberId());
        return resp;
    }

    @Override
    public Page<Order> searchOrders(LocalDate startDate, LocalDate endDate, Integer status, Integer paymentType, String keyword, Pageable pageable) {
        org.springframework.data.jpa.domain.Specification<Order> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("orderTime"), startDate.atStartOfDay()));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("orderTime"), endDate.plusDays(1).atStartOfDay()));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("paymentStatus"), status));
            }
            if (paymentType != null) {
                predicates.add(cb.equal(root.get("paymentType"), paymentType));
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                String k = keyword.trim();
                String likePattern = "%" + k + "%";

                List<jakarta.persistence.criteria.Predicate> keywordPredicates = new java.util.ArrayList<>();
                keywordPredicates.add(cb.like(root.get("customerName"), likePattern));
                keywordPredicates.add(cb.like(root.get("memberId"), likePattern));

                // 查找匹配的会员
                try {
                    List<Member> members = memberRepository.findByNameContainingOrPhoneContaining(k, k);
                    if (!members.isEmpty()) {
                        List<String> memberIds = members.stream().map(Member::getMemberId).collect(Collectors.toList());
                        keywordPredicates.add(root.get("memberId").in(memberIds));
                    }
                } catch (Exception e) {
                    // ignore
                }

                predicates.add(cb.or(keywordPredicates.toArray(new jakarta.persistence.criteria.Predicate[0])));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return orderRepository.findAll(spec, pageable);
    }

    // 异步触发会员统计刷新
    private void triggerMemberStatsUpdate(String memberId){
        if(memberId==null || memberId.isBlank()) return;
        try {
            if(memberConsumptionUpdater!=null){
                memberConsumptionUpdater.refreshSingleMember(memberId);
            } else {
                System.out.println("[MemberStats] 无成员消费刷新组件，跳过刷新，memberId="+memberId);
            }
        } catch(Exception e){
            System.err.println("[MemberStats] 刷新会员消费统计失败:"+e.getMessage());
        }
    }

    // 辅助方法：获取支付状态文本
    private String getPaymentStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待支付";
            case 1: return "已支付";
            case 2: return "已退款";
            default: return "未知状态(" + status + ")";
        }
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
        // 附加显示获得积分（如果实体包含该字段）
        try {
            java.lang.reflect.Method getter = order.getClass().getMethod("getCreatedPoints");
            Object cp = getter.invoke(order);
            if (cp instanceof Integer) {
                // 若 OrderResponse 有相应字段，可设置；否则忽略
                try {
                    java.lang.reflect.Method setCp = response.getClass().getMethod("setCreatedPoints", Integer.class);
                    setCp.invoke(response, (Integer) cp);
                } catch (NoSuchMethodException ignore) {}
            }
        } catch (Exception ignore) {}

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

    private Integer resolveCashierId() {
        // 优先找活动员工
        return employeeRepository.findAllActive().stream()
                .map(Employee::getEmployeeId)
                .findFirst()
                .orElseGet(() -> {
                    // 若无活动员工则尝试创建一个默认员工(只在极端情况下执行)
                    try {
                        Employee e = new Employee();
                        e.setUsername("auto_cashier");
                        e.setPassword("e10adc3949ba59abbe56e057f20f883e"); // 默认 md5(123456)
                        e.setName("自动收银员");
                        e.setRoleId(1); // 赋管理员或收银角色，根据实际需要调整
                        e.setPhone("00000000000");
                        e.setStatus(1);
                        Employee saved = employeeRepository.save(e);
                        return saved.getEmployeeId();
                    } catch (Exception ex) {
                        System.err.println("[Order] 自动创建收银员失败: " + ex.getMessage());
                        return null;
                    }
                });
    }
}
