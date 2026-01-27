package com.pharmacy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "`order`") // 使用反引号，因为order是SQL关键字
public class Order {
    @Id
    @Column(name = "order_id", length = 32)
    private String orderId;

    @Column(name = "cashier_id", nullable = false)
    private Integer cashierId;

    @Column(name = "member_id", length = 32)
    private String memberId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "discount_amount")
    private Double discountAmount = 0.0;

    @Column(name = "actual_payment", nullable = false)
    private Double actualPayment;

    @Column(name = "payment_type", nullable = false)
    private Integer paymentType;

    @Column(name = "payment_status", nullable = false)
    private Integer paymentStatus = 0;

    @Column(name = "used_points")
    private Integer usedPoints = 0;

    @Column(name = "created_points")
    private Integer createdPoints = 0;

    @Column(name = "order_time")
    private LocalDateTime orderTime;

    @Column(name = "pay_time")
    private LocalDateTime payTime;

    @Column(name = "remark")
    private String remark;

    // 构造方法
    public Order() {}

    // Getter 和 Setter 方法
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public Integer getCashierId() { return cashierId; }
    public void setCashierId(Integer cashierId) { this.cashierId = cashierId; }

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public Double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(Double discountAmount) { this.discountAmount = discountAmount; }

    public Double getActualPayment() { return actualPayment; }
    public void setActualPayment(Double actualPayment) { this.actualPayment = actualPayment; }

    public Integer getPaymentType() { return paymentType; }
    public void setPaymentType(Integer paymentType) { this.paymentType = paymentType; }

    public Integer getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(Integer paymentStatus) { this.paymentStatus = paymentStatus; }

    public Integer getUsedPoints() { return usedPoints; }
    public void setUsedPoints(Integer usedPoints) { this.usedPoints = usedPoints; }

    public Integer getCreatedPoints() { return createdPoints; }
    public void setCreatedPoints(Integer createdPoints) { this.createdPoints = createdPoints; }

    public LocalDateTime getOrderTime() { return orderTime; }
    public void setOrderTime(LocalDateTime orderTime) { this.orderTime = orderTime; }

    public LocalDateTime getPayTime() { return payTime; }
    public void setPayTime(LocalDateTime payTime) { this.payTime = payTime; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    @PrePersist
    public void prePersist() {
        if (this.orderTime == null) {
            this.orderTime = LocalDateTime.now();
        }
    }
}