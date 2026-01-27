// MedicineWithStockDTO.java
package com.pharmacy.dto;

import java.math.BigDecimal;

public class MedicineWithStockDTO {
    private String medicineId;
    private String genericName;
    private String tradeName;
    private String spec;
    private String manufacturer;
    private BigDecimal retailPrice;
    private BigDecimal memberPrice;
    private Boolean isRx;
    private String unit;
    private String description;
    private Integer stockQuantity;

    // 构造函数
    public MedicineWithStockDTO() {}

    // Getter 和 Setter 方法
    public String getMedicineId() { return medicineId; }
    public void setMedicineId(String medicineId) { this.medicineId = medicineId; }

    public String getGenericName() { return genericName; }
    public void setGenericName(String genericName) { this.genericName = genericName; }

    public String getTradeName() { return tradeName; }
    public void setTradeName(String tradeName) { this.tradeName = tradeName; }

    public String getSpec() { return spec; }
    public void setSpec(String spec) { this.spec = spec; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public BigDecimal getRetailPrice() { return retailPrice; }
    public void setRetailPrice(BigDecimal retailPrice) { this.retailPrice = retailPrice; }

    public BigDecimal getMemberPrice() { return memberPrice; }
    public void setMemberPrice(BigDecimal memberPrice) { this.memberPrice = memberPrice; }

    public Boolean getIsRx() { return isRx; }
    public void setIsRx(Boolean isRx) { this.isRx = isRx; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
}