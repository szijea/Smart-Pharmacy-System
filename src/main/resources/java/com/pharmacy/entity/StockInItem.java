package com.pharmacy.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "stock_in_item")
public class StockInItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @ManyToOne
    @JoinColumn(name = "stock_in_id", nullable = false)
    private StockIn stockIn;

    @ManyToOne
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;

    @Column(name = "batch_no", length = 50)
    private String batchNo;

    @Column(name = "expire_date")
    private java.sql.Date expireDate;

    // Getter和Setter
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public StockIn getStockIn() { return stockIn; }
    public void setStockIn(StockIn stockIn) { this.stockIn = stockIn; }
    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public java.sql.Date getExpireDate() { return expireDate; }
    public void setExpireDate(java.sql.Date expireDate) { this.expireDate = expireDate; }

    // 计算小计
    public Double getSubtotal() {
        return quantity * unitPrice;
    }
}