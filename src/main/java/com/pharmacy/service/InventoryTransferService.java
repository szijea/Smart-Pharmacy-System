package com.pharmacy.service;

import com.pharmacy.shared.entity.ProductTransferLog;
import com.pharmacy.multitenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import com.pharmacy.shared.service.ProductTransferLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class InventoryTransferService {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ProductTransferLogService productTransferLogService;

    @Autowired
    private com.pharmacy.repository.MedicineRepository medicineRepository;

    @Autowired
    private com.pharmacy.repository.CategoryRepository categoryRepository;

    public void transferStock(String fromTenant, String toTenant, String medicineId, String batchNo, int quantity, String operatorId, String remark) {
        // Map 'warehouse' to 'default' tenant
        if ("warehouse".equalsIgnoreCase(fromTenant)) { fromTenant = "default"; }
        if ("warehouse".equalsIgnoreCase(toTenant)) { toTenant = "default"; }

        String traceId = UUID.randomUUID().toString();

        // Step 1: Log the initiation of the transfer in the default database
        ProductTransferLog log = createInitialLog(fromTenant, toTenant, medicineId, batchNo, quantity, operatorId, remark, traceId);
        final String userRemark = log.getRemark();

        try {
            // Step 2: Deduct stock from the source tenant
            deductStockFromSource(fromTenant, medicineId, batchNo, quantity);

            try {
                // Step 3: Add stock to the destination tenant
                addStockToDestination(toTenant, fromTenant, medicineId, batchNo, quantity);

                // Step 4: Update log to success
                log.setRemark(limitRemark(mergeRemark(userRemark, "Transfer completed successfully.")));
                updateLog(log);

            } catch (Exception addEx) {
                // If adding stock fails, try to compensate by reverting the deduction
                log.setRemark(limitRemark(mergeRemark(userRemark, "Failed to add stock to destination. Attempting to roll back. Error: " + addEx.getMessage())));
                updateLog(log);

                try {
                    compensateSourceStock(fromTenant, medicineId, batchNo, quantity);
                    log.setRemark(limitRemark(mergeRemark(userRemark, "Rollback successful. Stock was returned to the source.")));
                    updateLog(log);
                } catch (Exception compensateEx) {
                    log.setRemark(limitRemark(mergeRemark(userRemark, "CRITICAL ERROR: Failed to roll back stock deduction. Manual intervention required. Error: " + compensateEx.getMessage())));
                    updateLog(log);
                }
                throw new RuntimeException("Failed to add stock to destination tenant: " + toTenant, addEx);
            }
        } catch (Exception deductEx) {
            log.setRemark(limitRemark(mergeRemark(userRemark, "Failed to deduct stock from source. Error: " + deductEx.getMessage())));
            updateLog(log);
            throw new RuntimeException("Failed to deduct stock from source tenant: " + fromTenant, deductEx);
        }
    }

    private ProductTransferLog createInitialLog(String from, String to, String medId, String batch, int qty, String opId, String remark, String traceId) {
        try {
            TenantContext.setCurrentTenant("default");
            ProductTransferLog log = new ProductTransferLog();
            log.setFromTenant(from);
            log.setToTenant(to);
            log.setMedicineId(medId);
            log.setBatchNo(batch);
            log.setQuantity(qty);
            log.setOperatorId(opId);
            String initialRemark = (remark != null && !remark.isBlank()) ? remark : "Transfer initiated.";
            log.setRemark(limitRemark(initialRemark));
            log.setTraceId(traceId);
            return productTransferLogService.saveLog(log);
        } finally {
            TenantContext.clear();
        }
    }

    private void updateLog(ProductTransferLog log) {
        try {
            TenantContext.setCurrentTenant("default");
            log.setRemark(limitRemark(log.getRemark()));
            productTransferLogService.saveLog(log);
        } finally {
            TenantContext.clear();
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void deductStockFromSource(String tenant, String medicineId, String batchNo, int quantity) {
        try {
            TenantContext.setCurrentTenant(tenant);
            boolean success = inventoryService.deductStockForTransfer(medicineId, batchNo, quantity);
            if (!success) {
                throw new RuntimeException("Insufficient stock or inventory not found.");
            }
        } finally {
            TenantContext.clear();
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void addStockToDestination(String tenant, String sourceTenant, String medicineId, String batchNo, int quantity) {
        try {
            TenantContext.setCurrentTenant(tenant);
            ensureMedicineExistsInTenant(medicineId, sourceTenant);
            inventoryService.addStockForTransfer(medicineId, batchNo, quantity);
        } finally {
            TenantContext.clear();
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void compensateSourceStock(String tenant, String medicineId, String batchNo, int quantity) {
        try {
            TenantContext.setCurrentTenant(tenant);
            inventoryService.addStockForTransfer(medicineId, batchNo, quantity);
        } finally {
            TenantContext.clear();
        }
    }

    private String limitRemark(String text){
        if(text == null) return null;
        if(text.length() <= 240) return text;
        return text.substring(0, 240);
    }

    private void ensureMedicineExistsInTenant(String medicineId, String sourceTenant) {
        // If medicine already exists in current tenant DB, nothing to do
        if (medicineRepository.existsById(medicineId)) {
            return;
        }

        // Try to copy the medicine definition from the source tenant (default to 'default' when missing)
        String targetTenant = TenantContext.getCurrentTenant();
        String effectiveSource = (sourceTenant == null || sourceTenant.isBlank()) ? "default" : sourceTenant;

        com.pharmacy.entity.Medicine source;
        try {
            TenantContext.setCurrentTenant(effectiveSource);
            source = medicineRepository.findById(medicineId).orElse(null);
        } finally {
            TenantContext.setCurrentTenant(targetTenant);
        }

        if (source == null) {
            throw new IllegalStateException("Medicine " + medicineId + " does not exist in source tenant " + effectiveSource + "; cannot transfer.");
        }

        Integer resolvedCategoryId = resolveCategoryInTarget(source.getCategoryId(), effectiveSource, targetTenant);

        com.pharmacy.entity.Medicine clone = new com.pharmacy.entity.Medicine();
        clone.setMedicineId(source.getMedicineId());
        clone.setGenericName(source.getGenericName());
        clone.setTradeName(source.getTradeName());
        clone.setProductCode(source.getProductCode());
        clone.setDosageForm(source.getDosageForm());
        clone.setSpec(source.getSpec());
        clone.setApprovalNo(source.getApprovalNo());
        clone.setCategoryId(resolvedCategoryId);
        clone.setManufacturer(source.getManufacturer());
        clone.setRetailPrice(source.getRetailPrice());
        clone.setMemberPrice(source.getMemberPrice());
        clone.setIsRx(source.getIsRx());
        clone.setBarcode(source.getBarcode());
        clone.setUnit(source.getUnit());
        clone.setDescription(source.getDescription());
        clone.setProductionDate(source.getProductionDate());
        clone.setExpiryDate(source.getExpiryDate());
        clone.setStatus(source.getStatus());
        clone.setUsageDosage(source.getUsageDosage());
        clone.setContraindication(source.getContraindication());
        clone.setDeleted(false);

        TenantContext.setCurrentTenant(targetTenant);
        medicineRepository.save(clone);
    }

    private Integer resolveCategoryInTarget(Integer sourceCategoryId, String sourceTenant, String targetTenant) {
        if (sourceCategoryId == null) {
            return null;
        }

        com.pharmacy.entity.Category sourceCategory;
        try {
            TenantContext.setCurrentTenant(sourceTenant);
            sourceCategory = categoryRepository.findById(sourceCategoryId).orElse(null);
        } finally {
            TenantContext.setCurrentTenant(targetTenant);
        }

        if (sourceCategory == null) {
            return null; // source tenant also missing category; skip to avoid failure
        }

        // Try to find by name in target; if missing, create a simple copy (parent/sort kept best-effort)
        return categoryRepository.findByCategoryNameIgnoreCase(sourceCategory.getCategoryName())
                .map(com.pharmacy.entity.Category::getCategoryId)
                .orElseGet(() -> {
                    com.pharmacy.entity.Category copy = new com.pharmacy.entity.Category();
                    copy.setCategoryName(sourceCategory.getCategoryName());
                    copy.setParentId(0);
                    copy.setSort(sourceCategory.getSort());
                    return categoryRepository.save(copy).getCategoryId();
                });
    }

    private String mergeRemark(String userRemark, String statusText) {
        if (userRemark == null || userRemark.isBlank()) {
            return statusText;
        }
        return userRemark + " | " + statusText;
    }
}
