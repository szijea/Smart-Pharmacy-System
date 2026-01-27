package com.pharmacy;

import com.pharmacy.entity.Medicine;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.service.MedicineService;
import com.pharmacy.service.InventoryService;
import com.pharmacy.dto.MedicineWithStockDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@SpringBootTest
public class MedicineUpdatePersistenceTest {

    @Autowired
    private MedicineRepository medicineRepository;
    @Autowired
    private MedicineService medicineService;
    @Autowired
    private InventoryService inventoryService;

    @Test
    @Transactional
    void testUpdateDatesPersistedAndVisibleInSearchWithStock() {
        String id = "TEST_MED" + System.currentTimeMillis();
        Medicine m = new Medicine();
        m.setMedicineId(id);
        m.setGenericName("测试药品");
        m.setTradeName("测试药品");
        m.setSpec("10片/盒");
        m.setApprovalNo("APP-ORIG");
        m.setCategoryId(2);
        m.setManufacturer("测试厂家");
        m.setRetailPrice(new BigDecimal("12.50"));
        m.setMemberPrice(new BigDecimal("10.00"));
        m.setIsRx(false);
        m.setUnit("盒");
        m.setDescription("初始描述");
        m.setProductionDate(LocalDate.of(2025,1,1));
        m.setExpiryDate(LocalDate.of(2026,1,1));
        m.setStatus("ACTIVE");
        medicineRepository.save(m);

        // 更新批准文号与日期
        Medicine update = new Medicine();
        update.setTradeName("测试药品修改");
        update.setApprovalNo("APP-NEW");
        update.setProductionDate(LocalDate.of(2025,2,2));
        update.setExpiryDate(LocalDate.of(2026,2,2));
        medicineService.updateMedicine(id, update);

        Medicine reloaded = medicineRepository.findById(id).orElseThrow();
        Assertions.assertEquals("APP-NEW", reloaded.getApprovalNo(), "批准文号更新后应保存");
        Assertions.assertEquals(LocalDate.of(2025,2,2), reloaded.getProductionDate(), "生产日期更新后应保存");
        Assertions.assertEquals(LocalDate.of(2026,2,2), reloaded.getExpiryDate(), "到期日期更新后应保存");

        // 通过含库存搜索验证 DTO 带出这些字段
        var page = medicineService.searchMedicinesWithStock(null, null, 1, 50);
        List<MedicineWithStockDTO> list = page.getContent();
        MedicineWithStockDTO dto = list.stream().filter(x -> id.equals(x.getMedicineId())).findFirst().orElse(null);
        Assertions.assertNotNull(dto, "搜索结果中应存在测试药品");
        Assertions.assertEquals("APP-NEW", dto.getApprovalNo(), "DTO 应包含更新后的批准文号");
        Assertions.assertEquals(LocalDate.of(2025,2,2), dto.getProductionDate(), "DTO 应包含更新后的生产日期");
        Assertions.assertEquals(LocalDate.of(2026,2,2), dto.getExpiryDate(), "DTO 应包含更新后的到期日期");
    }
}

