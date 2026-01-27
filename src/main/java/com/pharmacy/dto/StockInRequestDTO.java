// StockInRequestDTO.java
package com.pharmacy.dto;

import java.time.LocalDateTime;
import java.util.List;

public class StockInRequestDTO {
    private LocalDateTime stockInDate;
    private String remark;
    private Integer status;
    private Long supplierId;
    private List<StockInItemDTO> items;

    // 构造器、getter、setter
    public StockInRequestDTO() {}

    public LocalDateTime getStockInDate() { return stockInDate; }
    public void setStockInDate(LocalDateTime stockInDate) { this.stockInDate = stockInDate; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public List<StockInItemDTO> getItems() { return items; }
    public void setItems(List<StockInItemDTO> items) { this.items = items; }
}