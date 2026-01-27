package com.pharmacy.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderRequest {
    private String customerName;
    private String memberId; // 添加会员ID字段
    private List<OrderItemRequest> items;
    private String paymentMethod; // 支付方式
    private BigDecimal totalAmount; // 总金额
    private BigDecimal discountAmount; // 折扣金额
    private BigDecimal originalAmount; // 原始金额

    // 默认构造函数
    public OrderRequest() {}

    // 全参构造函数（更新，包含memberId）
    public OrderRequest(String customerName, String memberId, List<OrderItemRequest> items) {
        this.customerName = customerName;
        this.memberId = memberId;
        this.items = items;
    }

    // Getter 和 Setter 方法
    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public List<OrderItemRequest> getItems() {
        if (items == null) items = new ArrayList<>();
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }

    // 添加对应的getter和setter
    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }
}
