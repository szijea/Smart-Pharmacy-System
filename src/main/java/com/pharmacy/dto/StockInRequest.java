package com.pharmacy.dto;

import java.time.LocalDateTime;
import java.util.List;

public class StockInRequest {
    private String stockInNo;
    private LocalDateTime stockInDate;
    private String remark;
    private Long supplierId;
    private List<StockInItemRequest> items;

    // Getters and Setters
    public String getStockInNo() {
        return stockInNo;
    }

    public void setStockInNo(String stockInNo) {
        this.stockInNo = stockInNo;
    }

    public LocalDateTime getStockInDate() {
        return stockInDate;
    }

    public void setStockInDate(LocalDateTime stockInDate) {
        this.stockInDate = stockInDate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public List<StockInItemRequest> getItems() {
        return items;
    }

    public void setItems(List<StockInItemRequest> items) {
        this.items = items;
    }
}

