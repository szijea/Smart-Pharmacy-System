package com.pharmacy.entity;

import jakarta.persistence.*;  // 改为 jakarta
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "medicine")
public class Medicine {
    @Id
    @Column(name = "medicine_id", length = 32)
    private String medicineId;

    @Column(name = "generic_name", nullable = false, length = 100)
    private String genericName;

    @Column(name = "trade_name", length = 100)
    private String tradeName;

    @Column(length = 50)
    private String spec;

    @Column(name = "approval_no", nullable = false, length = 50)
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
    private Supplier supplier;

    // 构造方法、Getter和Setter保持不变
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

    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}