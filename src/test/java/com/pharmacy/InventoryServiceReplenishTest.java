package com.pharmacy;

import com.pharmacy.entity.Inventory;
import com.pharmacy.service.InventoryService;
import com.pharmacy.repository.InventoryRepository;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.entity.Medicine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class InventoryServiceReplenishTest {

    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private MedicineRepository medicineRepository;

    private Medicine ensureMedicine(String id){
        return medicineRepository.findById(id).orElseGet(() -> {
            Medicine m = new Medicine();
            m.setMedicineId(id);
            m.setGenericName(id+"-GENERIC");
            m.setTradeName(id+"-TRADE");
            m.setApprovalNo(id+"-APP");
            m.setCategoryId(1);
            m.setRetailPrice(new BigDecimal("10.00"));
            m.setMemberPrice(new BigDecimal("9.00"));
            m.setIsRx(false);
            m.setUnit("盒");
            return medicineRepository.save(m);
        });
    }

    @Test
    void testReplenishZeroUpdatesMinStockOnly() {
        String medicineId = "TEST-MED-001";
        ensureMedicine(medicineId);
        // 首次：创建批次（数量=0, 设置 minStock=50）
        Inventory first = inventoryService.replenish(medicineId, 0, "BATCH-ZERO", 50);
        assertNotNull(first);
        assertEquals(50, first.getMinStock());
        assertEquals(0, first.getStockQuantity());

        // 第二次：数量仍为0，只提升 minStock=80，库存不变
        Inventory second = inventoryService.replenish(medicineId, 0, "BATCH-ZERO", 80);
        assertNotNull(second);
        assertEquals(80, second.getMinStock());
        assertEquals(0, second.getStockQuantity());
    }

    @Test
    void testEarliestExpiryDateAggregation() {
        String medicineId = "TEST-MED-AGG";
        ensureMedicine(medicineId);
        // 创建三个不同的批次有效期
        inventoryService.createBatch(medicineId, "BATCH-A", 10, 5, null, null, LocalDate.now().plusDays(30), null);
        inventoryService.createBatch(medicineId, "BATCH-B", 5, 5, null, null, LocalDate.now().plusDays(15), null);
        inventoryService.createBatch(medicineId, "BATCH-C", 8, 5, null, null, LocalDate.now().plusDays(45), null);

        List<com.pharmacy.dto.InventoryDTO> list = inventoryService.findDTOByMedicineId(medicineId);
        assertFalse(list.isEmpty());
        LocalDate expectedEarliest = LocalDate.now().plusDays(15);
        for (com.pharmacy.dto.InventoryDTO dto : list) {
            assertEquals(expectedEarliest, dto.getEarliestExpiryDate(), "Earliest expiry should propagate to all DTOs");
        }
    }
}
