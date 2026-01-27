package com.pharmacy;

import com.pharmacy.entity.Medicine;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.service.MedicineService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@SpringBootTest
public class MedicineSoftDeleteTest {

    @Autowired
    private MedicineRepository medicineRepository;
    @Autowired
    private MedicineService medicineService;

    @Test
    @Transactional
    void testSoftDeleteMarksDeletedAndInactive() {
        String id = "TEST_SOFT_DEL" + System.currentTimeMillis();
        Medicine m = new Medicine();
        m.setMedicineId(id);
        m.setGenericName("软删药品");
        m.setTradeName("软删药品");
        m.setSpec("5片/盒");
        m.setApprovalNo("APP-SOFT-"+id);
        m.setCategoryId(2);
        m.setManufacturer("软删厂家");
        m.setRetailPrice(new BigDecimal("6.50"));
        m.setMemberPrice(new BigDecimal("5.50"));
        m.setIsRx(false);
        m.setUnit("盒");
        m.setStatus("ACTIVE");
        medicineRepository.save(m);

        // 执行软删除
        medicineService.deleteMedicine(id);
        Medicine deleted = medicineRepository.findById(id).orElseThrow();
        Assertions.assertTrue(Boolean.TRUE.equals(deleted.getDeleted()), "deleted 标志应为 true");
        Assertions.assertEquals("INACTIVE", deleted.getStatus(), "状态应被设置为 INACTIVE");
    }
}

