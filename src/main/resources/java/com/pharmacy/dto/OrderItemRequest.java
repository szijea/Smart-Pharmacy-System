package com.pharmacy.dto;

import java.math.BigDecimal;

public class OrderItemRequest {
    private String productId;
    private Integer quantity;
    private BigDecimal unitPrice;

    // 默认构造函数
    public OrderItemRequest() {}

    // 全参构造函数
    public OrderItemRequest(String productId, Integer quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Getter 和 Setter 方法
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}