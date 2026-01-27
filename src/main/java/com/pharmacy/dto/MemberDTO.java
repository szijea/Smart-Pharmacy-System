package com.pharmacy.dto;

import java.time.LocalDateTime;

public class MemberDTO {
    private String memberId;
    private String name;
    private String phone;
    private String cardNo;
    private Integer level;
    private Integer points;
    private String allergicHistory;
    private String medicalCardNo;
    private LocalDateTime createTime;

    // 等级显示名称
    private String levelName;

    // 用于前端显示的格式化时间
    private String createTimeFormatted;

    private Integer consumptionCount; // 已支付订单次数
    private java.time.LocalDateTime lastConsumptionDate; // 最近一次支付订单时间

    // 构造器
    public MemberDTO() {}

    public MemberDTO(String memberId, String name, String phone) {
        this.memberId = memberId;
        this.name = name;
        this.phone = phone;
    }

    // Getter 和 Setter 方法
    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCardNo() { return cardNo; }
    public void setCardNo(String cardNo) { this.cardNo = cardNo; }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }

    public String getAllergicHistory() { return allergicHistory; }
    public void setAllergicHistory(String allergicHistory) { this.allergicHistory = allergicHistory; }

    public String getMedicalCardNo() { return medicalCardNo; }
    public void setMedicalCardNo(String medicalCardNo) { this.medicalCardNo = medicalCardNo; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public String getLevelName() { return levelName; }
    public void setLevelName(String levelName) { this.levelName = levelName; }

    public String getCreateTimeFormatted() { return createTimeFormatted; }
    public void setCreateTimeFormatted(String createTimeFormatted) { this.createTimeFormatted = createTimeFormatted; }

    public Integer getConsumptionCount(){ return consumptionCount; }
    public void setConsumptionCount(Integer consumptionCount){ this.consumptionCount = consumptionCount; }
    public java.time.LocalDateTime getLastConsumptionDate(){ return lastConsumptionDate; }
    public void setLastConsumptionDate(java.time.LocalDateTime lastConsumptionDate){ this.lastConsumptionDate = lastConsumptionDate; }

    // 等级名称映射
    public static String getLevelName(Integer level) {
        if (level == null) return "普通会员";
        switch (level) {
            case 1: return "白银会员";
            case 2: return "黄金会员";
            case 3: return "铂金会员";
            case 4: return "VIP会员";
            default: return "普通会员";
        }
    }
}