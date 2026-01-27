// MedicineService.java - 完整版本
package com.pharmacy.service;

import com.pharmacy.entity.Medicine;
import com.pharmacy.dto.MedicineWithStockDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MedicineService {

    // 基本 CRUD 方法
    Page<Medicine> findAll(Pageable pageable);
    Optional<Medicine> findById(String medicineId);
    Medicine save(Medicine medicine);
    void deleteById(String medicineId);

    // 搜索方法
    List<Medicine> searchMedicines(String keyword);
    Page<Medicine> searchMedicines(String keyword, Pageable pageable);

    // 新增：分页搜索方法（包含分类）
    Page<Medicine> searchMedicines(String keyword, String category, int page, int size);

    // 分类相关方法
    Page<Medicine> findByCategory(String category, Pageable pageable);
    List<Medicine> getMedicinesByCategory(String category);

    // 获取所有药品
    Page<Medicine> getAllMedicines(Pageable pageable);

    // 处方药相关
    List<Medicine> getPrescriptionMedicines();
    List<Medicine> getNonPrescriptionMedicines();

    // 新增：包含库存的搜索方法
    Page<MedicineWithStockDTO> searchMedicinesWithStock(String keyword, String category, int page, int size);
    Page<MedicineWithStockDTO> getAllMedicinesWithStock(Pageable pageable);

    // 其他业务方法
    Medicine getMedicineById(String id);
    Medicine createMedicine(Medicine medicine);
    Medicine updateMedicine(String id, Medicine medicine);
    void deleteMedicine(String id);
}