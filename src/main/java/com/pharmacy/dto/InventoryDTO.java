package com.pharmacy.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class InventoryDTO {
    private Long inventoryId;
    private String batchNo;
    private LocalDateTime createTime;
    private LocalDate expiryDate;
    private Integer maxStock;
    private String medicineId; // changed to String
    private Integer minStock;
    private BigDecimal purchasePrice;
    private Integer stockQuantity;
    private String supplier;
    private LocalDateTime updateTime;

    private String medicineGenericName;
    private String medicineTradeName;
    private String medicineSpec;
    private BigDecimal medicineRetailPrice;

    private String stockStatus; // HIGH / MEDIUM / LOW / CRITICAL / OUT
    private String expiryStatus; // NORMAL / NEAR_EXPIRY / EXPIRED
    private LocalDate earliestExpiryDate; // 所有批次最早未过期日期
    private Integer safetyStock; // 展示用安全库存

    private Integer medicineCategoryId; // 新增：药品分类ID
    private Boolean medicineIsRx;       // 新增：是否处方药

    // Constructor used by JPQL constructor projection
    public InventoryDTO(Long inventoryId, String batchNo, LocalDateTime createTime, LocalDate expiryDate,
                        Integer maxStock, String medicineId, Integer minStock, BigDecimal purchasePrice,
                        Integer stockQuantity, String supplier, LocalDateTime updateTime,
                        String medicineGenericName, String medicineTradeName, String medicineSpec, BigDecimal medicineRetailPrice,
                        String stockStatus, String expiryStatus, LocalDate earliestExpiryDate, Integer safetyStock,
                        Integer medicineCategoryId, Boolean medicineIsRx) {
        this.inventoryId = inventoryId;
        this.batchNo = batchNo;
        this.createTime = createTime;
        this.expiryDate = expiryDate;
        this.maxStock = maxStock;
        this.medicineId = medicineId;
        this.minStock = minStock;
        this.purchasePrice = purchasePrice;
        this.stockQuantity = stockQuantity;
        this.supplier = supplier;
        this.updateTime = updateTime;
        this.medicineGenericName = medicineGenericName;
        this.medicineTradeName = medicineTradeName;
        this.medicineSpec = medicineSpec;
        this.medicineRetailPrice = medicineRetailPrice;
        this.stockStatus = stockStatus;
        this.expiryStatus = expiryStatus;
        this.earliestExpiryDate = earliestExpiryDate;
        this.safetyStock = safetyStock;
        this.medicineCategoryId = medicineCategoryId;
        this.medicineIsRx = medicineIsRx;
    }

    // 兼容旧的 JPQL 构造投影（无状态字段）
    public InventoryDTO(Long inventoryId, String batchNo, LocalDateTime createTime, LocalDate expiryDate,
                        Integer maxStock, String medicineId, Integer minStock, BigDecimal purchasePrice,
                        Integer stockQuantity, String supplier, LocalDateTime updateTime,
                        String medicineGenericName, String medicineTradeName, String medicineSpec, BigDecimal medicineRetailPrice) {
        this.inventoryId = inventoryId;
        this.batchNo = batchNo;
        this.createTime = createTime;
        this.expiryDate = expiryDate;
        this.maxStock = maxStock;
        this.medicineId = medicineId;
        this.minStock = minStock;
        this.purchasePrice = purchasePrice;
        this.stockQuantity = stockQuantity;
        this.supplier = supplier;
        this.updateTime = updateTime;
        this.medicineGenericName = medicineGenericName;
        this.medicineTradeName = medicineTradeName;
        this.medicineSpec = medicineSpec;
        this.medicineRetailPrice = medicineRetailPrice;
        // 新增状态字段默认填充
        this.stockStatus = null;
        this.expiryStatus = null;
        this.earliestExpiryDate = null;
        this.safetyStock = minStock;
        this.medicineCategoryId = null;
        this.medicineIsRx = null;
    }

    // Getters and setters
    public Long getInventoryId() { return inventoryId; }
    public void setInventoryId(Long inventoryId) { this.inventoryId = inventoryId; }

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public Integer getMaxStock() { return maxStock; }
    public void setMaxStock(Integer maxStock) { this.maxStock = maxStock; }

    public String getMedicineId() { return medicineId; }
    public void setMedicineId(String medicineId) { this.medicineId = medicineId; }

    public Integer getMinStock() { return minStock; }
    public void setMinStock(Integer minStock) { this.minStock = minStock; }

    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public String getMedicineGenericName() { return medicineGenericName; }
    public void setMedicineGenericName(String medicineGenericName) { this.medicineGenericName = medicineGenericName; }

    public String getMedicineTradeName() { return medicineTradeName; }
    public void setMedicineTradeName(String medicineTradeName) { this.medicineTradeName = medicineTradeName; }

    public String getMedicineSpec() { return medicineSpec; }
    public void setMedicineSpec(String medicineSpec) { this.medicineSpec = medicineSpec; }

    public BigDecimal getMedicineRetailPrice() { return medicineRetailPrice; }
    public void setMedicineRetailPrice(BigDecimal medicineRetailPrice) { this.medicineRetailPrice = medicineRetailPrice; }

    public String getStockStatus() { return stockStatus; }
    public void setStockStatus(String stockStatus) { this.stockStatus = stockStatus; }

    public String getExpiryStatus() { return expiryStatus; }
    public void setExpiryStatus(String expiryStatus) { this.expiryStatus = expiryStatus; }

    public LocalDate getEarliestExpiryDate() { return earliestExpiryDate; }
    public void setEarliestExpiryDate(LocalDate earliestExpiryDate) { this.earliestExpiryDate = earliestExpiryDate; }

    public Integer getSafetyStock() { return safetyStock; }
    public void setSafetyStock(Integer safetyStock) { this.safetyStock = safetyStock; }

    public Integer getMedicineCategoryId() { return medicineCategoryId; }
    public void setMedicineCategoryId(Integer medicineCategoryId) { this.medicineCategoryId = medicineCategoryId; }

    public Boolean getMedicineIsRx() { return medicineIsRx; }
    public void setMedicineIsRx(Boolean medicineIsRx) { this.medicineIsRx = medicineIsRx; }
}
