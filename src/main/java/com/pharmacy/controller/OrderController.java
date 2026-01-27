package com.pharmacy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacy.dto.OrderRequest;
import com.pharmacy.dto.OrderResponse;
import com.pharmacy.entity.Order;
import com.pharmacy.service.OrderService;
import com.pharmacy.repository.OrderItemRepository;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.entity.OrderItem;
import com.pharmacy.entity.Medicine;
import com.pharmacy.dto.OrderItemRequest;
import com.pharmacy.repository.MemberRepository;
import com.pharmacy.entity.Member;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired private MemberRepository memberRepository;

    @PostMapping(consumes = "*/*") // 接受任意 Content-Type，控制器内手动解析
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> createOrder(HttpServletRequest request,
                                         @RequestHeader(value="Content-Type", required=false) String contentType,
                                         @RequestHeader Map<String,String> allHeaders) {
        System.out.println("[OrderController] 收到创建订单请求 Content-Type=" + contentType + " headers=" + allHeaders);
        try {
            String bodyStr;
            try (var is = request.getInputStream()){
                bodyStr = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
            if(bodyStr.isBlank()){
                return ResponseEntity.badRequest().body(Map.of("code",400,"message","请求体为空"));
            }
            Map<String,Object> body = new ObjectMapper().readValue(bodyStr, Map.class);
            // 构造 OrderRequest（与先前逻辑一致）
            OrderRequest orderRequest = new OrderRequest();
            Object customerName = body.get("customerName");
            orderRequest.setCustomerName(customerName==null? "匿名客户"+System.currentTimeMillis(): String.valueOf(customerName));
            Object memberId = body.get("memberId");
            if(memberId!=null && !String.valueOf(memberId).isBlank()) orderRequest.setMemberId(String.valueOf(memberId));
            orderRequest.setPaymentMethod(String.valueOf(body.getOrDefault("paymentMethod","WECHAT")));
            orderRequest.setOriginalAmount(toBigDecimal(body.get("originalAmount")));
            orderRequest.setDiscountAmount(toBigDecimal(body.get("discountAmount")));
            orderRequest.setTotalAmount(toBigDecimal(body.get("totalAmount")));
            // items 解析
            java.util.List<OrderItemRequest> items = new java.util.ArrayList<>();
            Object rawItems = body.get("items");
            if(rawItems instanceof java.util.List<?> list){
                for(Object o: list){
                    Map<String,Object> map = (o instanceof Map<?,?>) ? (Map<String,Object>)o : new ObjectMapper().convertValue(o, Map.class);
                    //String/Long 兼容性：把 productId 解析为 String（兼容旧字段 medicineId）
                    Object rawPid = map.get("productId");
                    if (rawPid == null) rawPid = map.get("medicineId");
                    String productId = rawPid == null ? null : String.valueOf(rawPid);
                    Integer quantity = toInt(map.get("quantity"));
                    BigDecimal unitPrice = toBigDecimal(map.get("unitPrice"));
                    if(productId==null){
                        System.out.println("[OrderController] 跳过无 productId/medicineId 项:"+map);
                        continue;
                    }
                    if(quantity==null || quantity<=0) quantity = 1;
                    if(unitPrice==null) unitPrice = BigDecimal.ZERO;
                    OrderItemRequest itemReq = new OrderItemRequest();
                    itemReq.setProductId(productId);
                    itemReq.setQuantity(quantity);
                    itemReq.setUnitPrice(unitPrice);
                    items.add(itemReq);
                }
            }
            orderRequest.setItems(items);
            if(items.isEmpty()) return ResponseEntity.badRequest().body(Map.of("code",400,"message","订单项为空或未识别"));
            OrderResponse resp = orderService.createOrder(orderRequest);
            return ResponseEntity.ok(Map.of("code",200,"message","订单创建成功","orderId",resp.getOrderNumber(),"data",resp));
        } catch(Exception e){
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("code",500,"message","订单创建失败: "+e.getMessage()));
        }
    }
    private BigDecimal toBigDecimal(Object v){
        if(v==null) return null;
        if(v instanceof BigDecimal b) return b;
        if(v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try { return new BigDecimal(String.valueOf(v)); } catch(Exception e){ return null; }
    }
    private Integer toInt(Object v){ if(v==null) return null; if(v instanceof Number n) return n.intValue(); try { return Integer.parseInt(String.valueOf(v)); } catch(Exception e){ return null; } }
    private Long toLong(Object v){ if(v==null) return null; if(v instanceof Number n) return n.longValue(); try { return Long.parseLong(String.valueOf(v)); } catch(Exception e){ return null; } }
    private String val(java.util.Map<?,?> m, String k){ Object v=m.get(k); return v==null? null: String.valueOf(v); }

    @GetMapping("/debug-headers")
    public ResponseEntity<?> debugHeaders(@RequestHeader Map<String,String> headers){
        return ResponseEntity.ok(Map.of("code",200,"headers",headers));
    }

    @GetMapping("/ping")
    public Map<String,Object> ping(){
        return Map.of("code",200,"message","orders controller alive");
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable String orderId) {
        try {
            Optional<Order> orderOpt = orderService.getOrderByOrderId(orderId);
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                // 构建订单项列表并直接嵌入 data，前端 displayOrderDetail 读取 order.items
                List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
                java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();
                for (OrderItem oi : orderItems) {
                    java.util.Map<String, Object> itemMap = new java.util.HashMap<>();
                    itemMap.put("medicineId", oi.getMedicineId());
                    itemMap.put("quantity", oi.getQuantity());
                    itemMap.put("unitPrice", oi.getUnitPrice());
                    itemMap.put("subtotal", oi.getSubtotal());
                    Medicine med = medicineRepository.findById(oi.getMedicineId()).orElse(null);
                    if (med != null) {
                        itemMap.put("genericName", med.getGenericName());
                        itemMap.put("tradeName", med.getTradeName());
                        itemMap.put("spec", med.getSpec());
                        itemMap.put("manufacturer", med.getManufacturer());
                        itemMap.put("barcode", med.getBarcode());
                    } else {
                        itemMap.put("genericName", "未知药品");
                    }
                    items.add(itemMap);
                }
                Map<String,Object> orderMap = new HashMap<>();
                orderMap.put("orderId", order.getOrderId());
                orderMap.put("customerName", order.getCustomerName());
                orderMap.put("memberId", order.getMemberId());
                orderMap.put("cashierId", order.getCashierId());
                orderMap.put("totalAmount", order.getTotalAmount());
                orderMap.put("discountAmount", order.getDiscountAmount());
                orderMap.put("actualPayment", order.getActualPayment());
                orderMap.put("paymentType", order.getPaymentType());
                orderMap.put("paymentStatus", order.getPaymentStatus());
                orderMap.put("orderTime", order.getOrderTime());
                orderMap.put("payTime", order.getPayTime());
                orderMap.put("refundTime", order.getRefundTime());
                orderMap.put("refundReason", order.getRefundReason());
                orderMap.put("usedPoints", order.getUsedPoints());
                orderMap.put("createdPoints", order.getCreatedPoints());
                orderMap.put("items", items);
                if(order.getMemberId()!=null){
                    try { Member m = memberRepository.findById(order.getMemberId()).orElse(null); if(m!=null){ orderMap.put("memberName", m.getName()); } } catch(Exception ignored){}
                }
                return ResponseEntity.ok(Map.of("code",200,"message","success","data",orderMap));
            } else {
                return ResponseEntity.status(404).body(Map.of("code",404,"message","订单不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("code",500,"message","获取订单失败: "+e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Order> orders = orderService.getAllOrders(pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", orders.getContent());
            response.put("total", orders.getTotalElements());
            response.put("totalPages", orders.getTotalPages());
            response.put("currentPage", page);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("code",500,"message","获取订单列表失败: "+e.getMessage()));
        }
    }

    @GetMapping("/today")
    public ResponseEntity<?> getTodayOrders() {
        try {
            List<Order> orders = orderService.getTodayOrders();

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", orders);
            response.put("total", orders.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("code",500,"message","获取今日订单失败: "+e.getMessage()));
        }
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteOrder(@PathVariable String orderId) {
        try {
            boolean deleted = orderService.deleteOrder(orderId);
            if (deleted) {
                return ResponseEntity.ok(Map.of("code",200,"message","订单删除成功"));
            } else {
                return ResponseEntity.status(404).body(Map.of("code",404,"message","订单不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("code",500,"message","删除订单失败: "+e.getMessage()));
        }
    }

    // 新增：退单接口
    @PostMapping("/{orderId}/refund")
    public ResponseEntity<?> refund(@PathVariable String orderId, @RequestBody(required = false) Map<String,Object> body){
        try {
            String reason = body!=null && body.get("reason")!=null? String.valueOf(body.get("reason")) : "无";
            OrderResponse resp = orderService.refundOrder(orderId, reason);
            return ResponseEntity.ok(Map.of("code",200,"message","退单成功","data", resp));
        } catch (Exception e){
            return ResponseEntity.internalServerError().body(Map.of("code",500,"message","退单失败: "+e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchOrders(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer paymentType,
            @RequestParam(required = false) String memberKeyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        try {
            java.time.LocalDate start = null;
            java.time.LocalDate end = null;
            if (startDate != null && !startDate.isBlank()) {
                start = java.time.LocalDate.parse(startDate);
            }
            if (endDate != null && !endDate.isBlank()) {
                end = java.time.LocalDate.parse(endDate);
            }

            Pageable pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "orderTime"));
            Page<Order> orderPage = orderService.searchOrders(start, end, status, paymentType, memberKeyword, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("content", orderPage.getContent());
            response.put("totalElements", orderPage.getTotalElements());
            response.put("totalPages", orderPage.getTotalPages());
            response.put("page", page);
            response.put("size", size);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("code",500,"message","订单搜索失败: "+e.getMessage()));
        }
    }
}
