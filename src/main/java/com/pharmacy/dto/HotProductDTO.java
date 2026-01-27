// HotProductDTO.java
package com.pharmacy.dto;

import java.math.BigDecimal;

public class HotProductDTO {
    private String medicineId;
    private String medicineName;
    private String specification;
    private String category;
    private BigDecimal unitPrice;
    private Integer todaySales;
    private BigDecimal todayAmount;
    private Integer weekSales;
    private BigDecimal weekAmount;
    private Integer monthSales;
    private BigDecimal monthAmount;

    // Getter and Setter methods
    public String getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(String medicineId) {
        this.medicineId = medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getTodaySales() {
        return todaySales;
    }

    public void setTodaySales(Integer todaySales) {
        this.todaySales = todaySales;
    }

    public BigDecimal getTodayAmount() {
        return todayAmount;
    }

    public void setTodayAmount(BigDecimal todayAmount) {
        this.todayAmount = todayAmount;
    }

    public Integer getWeekSales() {
        return weekSales;
    }

    public void setWeekSales(Integer weekSales) {
        this.weekSales = weekSales;
    }

    public BigDecimal getWeekAmount() {
        return weekAmount;
    }

    public void setWeekAmount(BigDecimal weekAmount) {
        this.weekAmount = weekAmount;
    }

    public Integer getMonthSales() {
        return monthSales;
    }

    public void setMonthSales(Integer monthSales) {
        this.monthSales = monthSales;
    }

    public BigDecimal getMonthAmount() {
        return monthAmount;
    }

    public void setMonthAmount(BigDecimal monthAmount) {
        this.monthAmount = monthAmount;
    }
}