package com.pharmacy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_alert")
public class StockAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_id")
    private Long alertId;

    @Column(name = "medicine_id", length = 64, nullable = false)
    private String medicineId;

    @Column(name = "alert_type", nullable = false)
    private Integer alertType; // 1-库存不足 2-近效期 3-过期

    @Column(name = "current_stock")
    private Integer currentStock;

    @Column(name = "min_stock")
    private Integer minStock;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "alert_message")
    private String alertMessage;

    @Column(name = "is_handled")
    private Boolean isHandled = false;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    // 关联药品信息
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", insertable = false, updatable = false)
    private Medicine medicine;

    // 构造方法
    public StockAlert() {}

    // Getter和Setter
    public Long getAlertId() { return alertId; }
    public void setAlertId(Long alertId) { this.alertId = alertId; }

    public String getMedicineId() { return medicineId; }
    public void setMedicineId(String medicineId) { this.medicineId = medicineId; }

    public Integer getAlertType() { return alertType; }
    public void setAlertType(Integer alertType) { this.alertType = alertType; }

    public Integer getCurrentStock() { return currentStock; }
    public void setCurrentStock(Integer currentStock) { this.currentStock = currentStock; }

    public Integer getMinStock() { return minStock; }
    public void setMinStock(Integer minStock) { this.minStock = minStock; }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

    public String getAlertMessage() { return alertMessage; }
    public void setAlertMessage(String alertMessage) { this.alertMessage = alertMessage; }

    public Boolean getIsHandled() { return isHandled; }
    public void setIsHandled(Boolean isHandled) { this.isHandled = isHandled; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }

    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
    }
}