package com.pharmacy.dto;

import java.math.BigDecimal;

public class DashboardStatsDTO {
    private BigDecimal todaySales;
    private Long todayOrders;
    private BigDecimal yesterdaySales;
    private Long lowStockCount;
    private Long expiringSoonCount;
    private Long totalMedicines; // 改为Long类型
    private Long totalMembers;   // 改为Long类型
    private Integer memberConsumption;
    // 在DashboardStatsDTO中添加缺失的getter方法
    public BigDecimal getTodaySales() { return todaySales; }
    public Long getTodayOrders() { return todayOrders; }
    public BigDecimal getYesterdaySales() { return yesterdaySales; }
    public Long getLowStockCount() { return lowStockCount; }
    public Long getExpiringSoonCount() { return expiringSoonCount; }
    public Long getTotalMedicines() { return totalMedicines; }
    public Long getTotalMembers() { return totalMembers; }
    public Integer getMemberConsumption() { return memberConsumption; }
    // 添加所有字段的setter和getter
    public void setTodaySales(BigDecimal todaySales) {
        this.todaySales = todaySales;
    }

    public void setTodayOrders(Long todayOrders) {
        this.todayOrders = todayOrders;
    }

    public void setYesterdaySales(BigDecimal yesterdaySales) {
        this.yesterdaySales = yesterdaySales;
    }

    public void setLowStockCount(Long lowStockCount) {
        this.lowStockCount = lowStockCount;
    }

    public void setExpiringSoonCount(Long expiringSoonCount) {
        this.expiringSoonCount = expiringSoonCount;
    }

    public void setTotalMedicines(Long totalMedicines) {
        this.totalMedicines = totalMedicines;
    }

    public void setTotalMembers(Long totalMembers) {
        this.totalMembers = totalMembers;
    }

    public void setMemberConsumption(Integer memberConsumption) {
        this.memberConsumption = memberConsumption;
    }

    // getter方法省略（需补充）
}