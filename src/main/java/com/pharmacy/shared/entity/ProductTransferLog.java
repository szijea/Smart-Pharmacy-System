package com.pharmacy.shared.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_transfer_log")
public class ProductTransferLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_tenant", nullable = false)
    private String fromTenant;

    @Column(name = "to_tenant", nullable = false)
    private String toTenant;

    @Column(name = "medicine_id", nullable = false)
    private String medicineId;

    @Column(name = "batch_no")
    private String batchNo;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "operator_id")
    private String operatorId;

    @Column
    private String remark;

    @Column(name = "transfer_time", nullable = false)
    private LocalDateTime transferTime;

    @Column(name = "trace_id")
    private String traceId;

    public ProductTransferLog() {
        this.transferTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFromTenant() {
        return fromTenant;
    }

    public void setFromTenant(String fromTenant) {
        this.fromTenant = fromTenant;
    }

    public String getToTenant() {
        return toTenant;
    }

    public void setToTenant(String toTenant) {
        this.toTenant = toTenant;
    }

    public String getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(String medicineId) {
        this.medicineId = medicineId;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getTransferTime() {
        return transferTime;
    }

    public void setTransferTime(LocalDateTime transferTime) {
        this.transferTime = transferTime;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
