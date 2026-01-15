package com.pharmacy.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long id;

    @Column(name = "medicine_id", nullable = false, length = 64)
    private String medicineId;

    @Column(name = "batch_no", nullable = false, length = 50)
    private String batchNo;

    // 修复：使用正确的库存列 stock_quantity
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "min_stock")
    private Integer minStock;

    @Column(name = "max_stock")
    private Integer maxStock;

    @Column(name = "purchase_price", precision = 10, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "warehouse", length = 50)
    private String warehouse;

    @Column(name = "inbound_date")
    private LocalDateTime inboundDate;

    @Column(name = "acceptance_no", length = 50)
    private String acceptanceNo;

    @Column(name = "invoice_no", length = 50)
    private String invoiceNo;

    @Column(name = "invoice_date")
    private LocalDateTime invoiceDate;

    @Column(name = "acceptance_date")
    private LocalDateTime acceptanceDate;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "supplier", length = 100)
    private String supplier;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    // 关联药品信息
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", referencedColumnName = "medicine_id", insertable = false, updatable = false)
    private Medicine medicine;

    // 构造方法
    public Inventory() {}

    public Inventory(String medicineId, String batchNo, Integer stockQuantity, LocalDate expiryDate) {
        this.medicineId = medicineId;
        this.batchNo = batchNo;
        this.stockQuantity = stockQuantity;
        this.expiryDate = expiryDate;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    // Getter 和 Setter 方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMedicineId() { return medicineId; }
    public void setMedicineId(String medicineId) { this.medicineId = medicineId; }

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

    // 修复：使用 stockQuantity 而不是 quantity
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public Integer getMinStock() { return minStock; }
    public void setMinStock(Integer minStock) { this.minStock = minStock; }

    public Integer getMaxStock() { return maxStock; }
    public void setMaxStock(Integer maxStock) { this.maxStock = maxStock; }

    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public String getWarehouse() { return warehouse; }
    public void setWarehouse(String warehouse) { this.warehouse = warehouse; }

    public LocalDateTime getInboundDate() { return inboundDate; }
    public void setInboundDate(LocalDateTime inboundDate) { this.inboundDate = inboundDate; }

    public String getAcceptanceNo() { return acceptanceNo; }
    public void setAcceptanceNo(String acceptanceNo) { this.acceptanceNo = acceptanceNo; }

    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }

    public LocalDateTime getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDateTime invoiceDate) { this.invoiceDate = invoiceDate; }

    public LocalDateTime getAcceptanceDate() { return acceptanceDate; }
    public void setAcceptanceDate(LocalDateTime acceptanceDate) { this.acceptanceDate = acceptanceDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
    }

    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }

    // 业务方法 - 修复：使用 stockQuantity
    public boolean isLowStock() {
        return minStock != null && stockQuantity != null && stockQuantity <= minStock;
    }

    public boolean isExpiringSoon() {
        return expiryDate != null &&
                expiryDate.isBefore(LocalDate.now().plusMonths(3)) &&
                expiryDate.isAfter(LocalDate.now());
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }
}

