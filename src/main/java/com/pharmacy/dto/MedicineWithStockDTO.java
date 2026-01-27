// MedicineWithStockDTO.java
package com.pharmacy.dto;

import java.math.BigDecimal;
import java.util.List;

public class MedicineWithStockDTO {
    // changed to String to match Medicine.medicineId
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
    private List<String> batchNos; // 新增：批号列表
    private java.time.LocalDate earliestExpiryDate; // 新增：最早未过期日期
    private String approvalNo;
    private String barcode;
    private java.time.LocalDate productionDate;
    private java.time.LocalDate expiryDate;
    private String status;
    private String expiryStatus; // NORMAL / NEAR_EXPIRY / EXPIRED
    private String stockStatus;  // HIGH / MEDIUM / LOW / CRITICAL / OUT
    private String usageDosage;
    private String contraindication;

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

    public List<String> getBatchNos() { return batchNos; }
    public void setBatchNos(List<String> batchNos) { this.batchNos = batchNos; }

    public java.time.LocalDate getEarliestExpiryDate() { return earliestExpiryDate; }
    public void setEarliestExpiryDate(java.time.LocalDate earliestExpiryDate) { this.earliestExpiryDate = earliestExpiryDate; }

    public String getApprovalNo() { return approvalNo; }
    public void setApprovalNo(String approvalNo) { this.approvalNo = approvalNo; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public java.time.LocalDate getProductionDate() { return productionDate; }
    public void setProductionDate(java.time.LocalDate productionDate) { this.productionDate = productionDate; }

    public java.time.LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(java.time.LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getExpiryStatus() { return expiryStatus; }
    public void setExpiryStatus(String expiryStatus) { this.expiryStatus = expiryStatus; }

    public String getStockStatus() { return stockStatus; }
    public void setStockStatus(String stockStatus) { this.stockStatus = stockStatus; }

    public String getUsageDosage() { return usageDosage; }
    public void setUsageDosage(String usageDosage) { this.usageDosage = usageDosage; }

    public String getContraindication() { return contraindication; }
    public void setContraindication(String contraindication) { this.contraindication = contraindication; }
}