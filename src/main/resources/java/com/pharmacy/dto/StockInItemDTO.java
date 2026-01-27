package com.pharmacy.dto;

public class StockInItemDTO {
    private Long itemId;
    private String medicineId;
    private String medicineName;
    private Integer quantity;
    private Double unitPrice;
    private String batchNo;
    private java.sql.Date expireDate;
    private Double subtotal;

    // 构造器、getter、setter
    public StockInItemDTO() {}

    public Double getSubtotal() {
        return quantity * unitPrice;
    }
    // 省略其他getter和setter...
}