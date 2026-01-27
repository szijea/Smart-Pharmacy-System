// StockInItem.java - 修复版本
package com.pharmacy.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "stock_in_item")
public class StockInItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_in_id", nullable = false)
    @JsonBackReference
    private StockIn stockIn;

    // FK column stored as String to match Medicine.medicineId
    @Column(name = "medicine_id", length = 64, nullable = false)
    private String medicineId;

    // ManyToOne mapping to Medicine is kept but not insertable/updatable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", referencedColumnName = "medicine_id", insertable = false, updatable = false)
    private Medicine medicine;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "production_date")
    private java.time.LocalDate productionDate;

    @Column(name = "expiry_date")
    private java.time.LocalDate expiryDate;

    // Getter和Setter
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public StockIn getStockIn() { return stockIn; }
    public void setStockIn(StockIn stockIn) { this.stockIn = stockIn; }

    public String getMedicineId() { return medicineId; }
    public void setMedicineId(String medicineId) { this.medicineId = medicineId; }

    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; if (medicine!=null) this.medicineId = medicine.getMedicineId(); }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }
    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    public java.time.LocalDate getProductionDate() { return productionDate; }
    public void setProductionDate(java.time.LocalDate productionDate) { this.productionDate = productionDate; }
    public java.time.LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(java.time.LocalDate expiryDate) { this.expiryDate = expiryDate; }

    // 计算小计
    public Double getSubtotal() {
        return (quantity==null?0:quantity) * (unitPrice==null?0.0:unitPrice);
    }
}