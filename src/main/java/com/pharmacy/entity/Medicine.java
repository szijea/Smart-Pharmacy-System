package com.pharmacy.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "medicine")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Medicine {
    @Id
    @Column(name = "medicine_id", length = 64)
    private String medicineId;

    @Column(name = "generic_name", nullable = false, length = 100)
    private String genericName;

    @Column(name = "trade_name", length = 100)
    private String tradeName;

    @Column(length = 50)
    private String spec;

    @Column(name = "approval_no", nullable = false, length = 50, unique = true)
    private String approvalNo;

    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    @Column(length = 100)
    private String manufacturer;

    @Column(name = "retail_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal retailPrice;

    @Column(name = "member_price", precision = 10, scale = 2)
    private BigDecimal memberPrice;

    @Column(name = "is_rx", nullable = false)
    private Boolean isRx = false;

    @Column(length = 20)
    private String unit;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    @JsonIgnore
    private Supplier supplier;

    @Column(name = "barcode", length = 64)
    private String barcode;

    @Column(name = "production_date")
    private java.time.LocalDate productionDate;

    @Column(name = "expiry_date")
    private java.time.LocalDate expiryDate;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "usage_dosage", length = 255)
    private String usageDosage;

    @Column(name = "contraindication", length = 255)
    private String contraindication;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    public Medicine() {}

    public String getMedicineId() { return medicineId; }
    public void setMedicineId(String medicineId) { this.medicineId = medicineId; }

    public String getGenericName() { return genericName; }
    public void setGenericName(String genericName) { this.genericName = genericName; }

    public String getTradeName() { return tradeName; }
    public void setTradeName(String tradeName) { this.tradeName = tradeName; }

    public String getSpec() { return spec; }
    public void setSpec(String spec) { this.spec = spec; }

    public String getApprovalNo() { return approvalNo; }
    public void setApprovalNo(String approvalNo) { this.approvalNo = approvalNo; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

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

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public java.time.LocalDate getProductionDate() { return productionDate; }
    public void setProductionDate(java.time.LocalDate productionDate) { this.productionDate = productionDate; }

    public java.time.LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(java.time.LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUsageDosage() { return usageDosage; }
    public void setUsageDosage(String usageDosage) { this.usageDosage = usageDosage; }

    public String getContraindication() { return contraindication; }
    public void setContraindication(String contraindication) { this.contraindication = contraindication; }

    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }

    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        if (this.status == null || (this.status instanceof String && ((String)this.status).isBlank())) {
            this.status = "ACTIVE";
        }
        if (this.deleted == null) {
            this.deleted = false;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
        if (this.status == null || this.status.isBlank()) {
            this.status = "ACTIVE";
        }
    }
}