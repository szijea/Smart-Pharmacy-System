package com.pharmacy.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "hang_order_item")
public class HangOrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "hang_id", nullable = false, length = 32)
    private String hangId;

    @Column(name = "medicine_id", nullable = false, length = 32)
    private String medicineId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    // 关联药品信息
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", insertable = false, updatable = false)
    private Medicine medicine;

    // 关联挂单信息
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hang_id", insertable = false, updatable = false)
    private HangOrder hangOrder;

    // 构造方法、Getter和Setter
    public HangOrderItem() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getHangId() { return hangId; }
    public void setHangId(String hangId) { this.hangId = hangId; }

    public String getMedicineId() { return medicineId; }
    public void setMedicineId(String medicineId) { this.medicineId = medicineId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }

    public HangOrder getHangOrder() { return hangOrder; }
    public void setHangOrder(HangOrder hangOrder) { this.hangOrder = hangOrder; }
}