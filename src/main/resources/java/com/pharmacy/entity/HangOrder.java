package com.pharmacy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hang_order")
public class HangOrder {
    @Id
    @Column(name = "hang_id", length = 32)
    private String hangId;

    @Column(name = "cashier_id", nullable = false)
    private Integer cashierId;

    @Column(name = "hang_time")
    private LocalDateTime hangTime;

    @Column(name = "status")
    private Integer status = 0;

    @Column(name = "remark", length = 200)
    private String remark;

    // 关联员工信息
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cashier_id", insertable = false, updatable = false)
    private Employee cashier;

    // 构造方法、Getter和Setter
    public HangOrder() {}

    public String getHangId() { return hangId; }
    public void setHangId(String hangId) { this.hangId = hangId; }

    public Integer getCashierId() { return cashierId; }
    public void setCashierId(Integer cashierId) { this.cashierId = cashierId; }

    public LocalDateTime getHangTime() { return hangTime; }
    public void setHangTime(LocalDateTime hangTime) { this.hangTime = hangTime; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public Employee getCashier() { return cashier; }
    public void setCashier(Employee cashier) { this.cashier = cashier; }

    @PrePersist
    public void prePersist() {
        if (this.hangTime == null) {
            this.hangTime = LocalDateTime.now();
        }
    }
}