package com.pharmacy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hang_order")
public class HangOrder {
    @Id
    @Column(name = "hang_id", length = 32)
    private String hangId;

    @Column(name = "hang_time")
    private LocalDateTime hangTime;

    @Column(name = "cart_json", columnDefinition = "TEXT")
    private String cartJson;

    @Column(name = "member_id", length = 32)
    private String memberId;

    @Column(name = "member_name", length = 50)
    private String memberName;

    @Column(name = "cashier_id")
    private Integer cashierId;

    @Column(name = "remark")
    private String remark;

    @Column(name = "status")
    private Integer status;

    public String getHangId() { return hangId; }
    public void setHangId(String hangId) { this.hangId = hangId; }

    public LocalDateTime getHangTime() { return hangTime; }
    public void setHangTime(LocalDateTime hangTime) { this.hangTime = hangTime; }

    public String getCartJson() { return cartJson; }
    public void setCartJson(String cartJson) { this.cartJson = cartJson; }

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public Integer getCashierId() { return cashierId; }
    public void setCashierId(Integer cashierId) { this.cashierId = cashierId; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
