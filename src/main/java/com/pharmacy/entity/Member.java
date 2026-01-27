package com.pharmacy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "`member`")  // 使用复数形式避免MySQL保留字冲突
public class Member {
    @Id
    @Column(name = "member_id", length = 32)
    private String memberId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "card_no", length = 50)
    private String cardNo;

    @Column(name = "level")
    private Integer level = 0;

    @Column(name = "points")
    private Integer points = 0;

    @Column(name = "allergic_history", columnDefinition = "TEXT")
    private String allergicHistory;

    @Column(name = "medical_card_no", length = 50)
    private String medicalCardNo;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    // 无参构造器
    public Member() {}

    // 业务构造器
    public Member(String memberId, String name, String phone) {
        this.memberId = memberId;
        this.name = name;
        this.phone = phone;
    }

    // Getter 方法
    public String getMemberId() {
        return memberId;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getCardNo() {
        return cardNo;
    }

    public Integer getLevel() {
        return level;
    }

    public Integer getPoints() {
        return points;
    }

    public String getAllergicHistory() {
        return allergicHistory;
    }

    public String getMedicalCardNo() {
        return medicalCardNo;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    // Setter 方法
    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public void setAllergicHistory(String allergicHistory) {
        this.allergicHistory = allergicHistory;
    }

    public void setMedicalCardNo(String medicalCardNo) {
        this.medicalCardNo = medicalCardNo;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    // 业务方法 - 增加积分
    public void addPoints(int pointsToAdd) {
        if (pointsToAdd > 0) {
            this.points += pointsToAdd;
        }
    }

    // 业务方法 - 使用积分
    public boolean usePoints(int pointsToUse) {
        if (pointsToUse > 0 && this.points >= pointsToUse) {
            this.points -= pointsToUse;
            return true;
        }
        return false;
    }

    // 生命周期回调
    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        // 初始化默认值
        if (level == null) {
            level = 0;
        }
        if (points == null) {
            points = 0;
        }
    }

    @Override
    public String toString() {
        return "Member{" +
                "memberId='" + memberId + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", cardNo='" + cardNo + '\'' +
                ", level=" + level +
                ", points=" + points +
                ", allergicHistory='" + (allergicHistory != null ? "有" : "无") + '\'' +
                ", medicalCardNo='" + medicalCardNo + '\'' +
                ", createTime=" + createTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return memberId != null && memberId.equals(member.memberId);
    }

    @Override
    public int hashCode() {
        return memberId != null ? memberId.hashCode() : 0;
    }
}