package com.pharmacy.dto;

public class CurrentStockDTO {
    private String medicineId;
    private String genericName;
    private String tradeName;
    private String batchNo; // Added
    private Integer currentStock;

    public CurrentStockDTO() {}
    public CurrentStockDTO(String medicineId, String genericName, String tradeName, Integer currentStock) {
        this(medicineId, genericName, tradeName, currentStock, null);
    }

    public CurrentStockDTO(String medicineId, String genericName, String tradeName, Integer currentStock, String batchNo) {
        this.medicineId = medicineId;
        this.genericName = genericName;
        this.tradeName = tradeName;
        this.currentStock = currentStock;
        this.batchNo = batchNo;
    }

    public String getMedicineId() { return medicineId; }
    public void setMedicineId(String medicineId) { this.medicineId = medicineId; }
    public String getGenericName() { return genericName; }
    public void setGenericName(String genericName) { this.genericName = genericName; }
    public String getTradeName() { return tradeName; }
    public void setTradeName(String tradeName) { this.tradeName = tradeName; }
    public String getBatchNo() { return batchNo; } // Added
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; } // Added
    public Integer getCurrentStock() { return currentStock; }
    public void setCurrentStock(Integer currentStock) { this.currentStock = currentStock; }
}
