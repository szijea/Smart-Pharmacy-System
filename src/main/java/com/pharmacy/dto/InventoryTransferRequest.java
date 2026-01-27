package com.pharmacy.dto;

public class InventoryTransferRequest {

    private String fromTenant;
    private String toTenant;
    private String medicineId;
    private String batchNo;
    private int quantity;
    private String operatorId;
    private String remark;

    // Getters and Setters
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
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
}
