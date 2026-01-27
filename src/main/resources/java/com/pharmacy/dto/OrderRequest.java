package com.pharmacy.dto;

import java.util.List;

public class OrderRequest {
    private String customerName;
    private List<OrderItemRequest> items;

    // 默认构造函数
    public OrderRequest() {}

    // 全参构造函数
    public OrderRequest(String customerName, List<OrderItemRequest> items) {
        this.customerName = customerName;
        this.items = items;
    }

    // Getter 和 Setter 方法
    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }
}