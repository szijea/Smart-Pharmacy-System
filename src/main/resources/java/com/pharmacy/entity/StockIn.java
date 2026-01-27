package com.pharmacy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stock_in")
public class StockIn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_in_id")
    private Long stockInId;

    @Column(name = "stock_in_no", nullable = false, unique = true, length = 32)
    private String stockInNo;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(name = "stock_in_date", nullable = false)
    private LocalDateTime stockInDate;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "operator_id")
    private Integer operatorId;

    @Column(name = "status")
    private Integer status = 0; // 0-待审核, 1-已入库, 2-已取消

    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @OneToMany(mappedBy = "stockIn", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StockInItem> items = new ArrayList<>();

    // 构造器
    public StockIn() {}

    public StockIn(String stockInNo, Supplier supplier, LocalDateTime stockInDate) {
        this.stockInNo = stockInNo;
        this.supplier = supplier;
        this.stockInDate = stockInDate;
    }

    // Getter和Setter
    public Long getStockInId() { return stockInId; }
    public void setStockInId(Long stockInId) { this.stockInId = stockInId; }
    public String getStockInNo() { return stockInNo; }
    public void setStockInNo(String stockInNo) { this.stockInNo = stockInNo; }
    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
    public LocalDateTime getStockInDate() { return stockInDate; }
    public void setStockInDate(LocalDateTime stockInDate) { this.stockInDate = stockInDate; }
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public Integer getOperatorId() { return operatorId; }
    public void setOperatorId(Integer operatorId) { this.operatorId = operatorId; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public List<StockInItem> getItems() { return items; }
    public void setItems(List<StockInItem> items) { this.items = items; }

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (stockInNo == null) {
            stockInNo = "SI" + System.currentTimeMillis();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    // 计算总金额
    public void calculateTotalAmount() {
        if (items != null) {
            this.totalAmount = items.stream()
                    .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                    .sum();
        }
    }
}