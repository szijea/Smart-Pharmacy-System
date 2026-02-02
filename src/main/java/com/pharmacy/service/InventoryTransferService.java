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

    public void transferStock(String fromTenant, String toTenant, String medicineId, String batchNo, int quantity, String operatorId, String remark) {
        // Map 'warehouse' to 'default' tenant
        if ("warehouse".equalsIgnoreCase(fromTenant)) { fromTenant = "default"; }
        if ("warehouse".equalsIgnoreCase(toTenant)) { toTenant = "default"; }

        String traceId = UUID.randomUUID().toString();

        // Step 1: Log the initiation of the transfer in the default database
        ProductTransferLog log = createInitialLog(fromTenant, toTenant, medicineId, batchNo, quantity, operatorId, remark, traceId);

        try {
            // Step 2: Deduct stock from the source tenant
            deductStockFromSource(fromTenant, medicineId, batchNo, quantity);

            try {
                // Step 3: Add stock to the destination tenant
                addStockToDestination(toTenant, medicineId, batchNo, quantity);

                // Step 4: Update log to success
                log.setRemark("Transfer completed successfully.");
                updateLog(log);

            } catch (Exception addEx) {
                // If adding stock fails, try to compensate by reverting the deduction
                log.setRemark("Failed to add stock to destination. Attempting to roll back. Error: " + addEx.getMessage());
                updateLog(log);

                try {
                    compensateSourceStock(fromTenant, medicineId, batchNo, quantity);
                    log.setRemark("Rollback successful. Stock was returned to the source.");
                    updateLog(log);
                } catch (Exception compensateEx) {
                    log.setRemark("CRITICAL ERROR: Failed to roll back stock deduction. Manual intervention required. Error: " + compensateEx.getMessage());
                    updateLog(log);
                }
                throw new RuntimeException("Failed to add stock to destination tenant: " + toTenant, addEx);
            }
        } catch (Exception deductEx) {
            log.setRemark("Failed to deduct stock from source. Error: " + deductEx.getMessage());
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
            log.setRemark("Transfer initiated.");
            log.setTraceId(traceId);
            return productTransferLogService.saveLog(log);
        } finally {
            TenantContext.clear();
        }
    }

    private void updateLog(ProductTransferLog log) {
        try {
            TenantContext.setCurrentTenant("default");
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
    private void addStockToDestination(String tenant, String medicineId, String batchNo, int quantity) {
        try {
            TenantContext.setCurrentTenant(tenant);
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
}
