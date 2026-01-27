// MedicineController.java - 修复版本
package com.pharmacy.controller;

import com.pharmacy.dto.MedicineWithStockDTO;
import com.pharmacy.entity.Medicine;
import com.pharmacy.service.InventoryService;
import com.pharmacy.service.MedicineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medicines")
public class MedicineController {

    @Autowired
    private MedicineService medicineService;

    @Autowired // 添加这行
    private InventoryService inventoryService;

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchMedicines(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100") int size) {

        System.out.println("=== 接收搜索请求 ===");
        System.out.println("keyword: " + keyword);
        System.out.println("category: " + category);
        System.out.println("page: " + page);
        System.out.println("size: " + size);

        try {
            Page<Medicine> medicinePage = medicineService.searchMedicines(keyword, category, page, size);
            List<Medicine> medicines = medicinePage.getContent();

            System.out.println("搜索结果数量: " + medicines.size());

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", medicines);
            response.put("total", medicinePage.getTotalElements());
            response.put("currentPage", page);
            response.put("totalPages", medicinePage.getTotalPages());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("搜索出错: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "搜索失败: " + e.getMessage());
            response.put("data", List.of());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping
    public ResponseEntity<Page<Medicine>> getAllMedicines(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Medicine> medicines = medicineService.getAllMedicines(pageable);
        return ResponseEntity.ok(medicines);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Medicine> getMedicineById(@PathVariable String id) {
        Medicine medicine = medicineService.getMedicineById(id);
        if (medicine != null) {
            return ResponseEntity.ok(medicine);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Medicine> createMedicine(@RequestBody Medicine medicine) {
        Medicine createdMedicine = medicineService.createMedicine(medicine);
        return ResponseEntity.ok(createdMedicine);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Medicine> updateMedicine(@PathVariable String id, @RequestBody Medicine medicine) {
        Medicine updatedMedicine = medicineService.updateMedicine(id, medicine);
        if (updatedMedicine != null) {
            return ResponseEntity.ok(updatedMedicine);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedicine(@PathVariable String id) {
        medicineService.deleteMedicine(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Medicine>> getMedicinesByCategory(@PathVariable String category) {
        List<Medicine> medicines = medicineService.getMedicinesByCategory(category);
        return ResponseEntity.ok(medicines);
    }

    @GetMapping("/prescription")
    public ResponseEntity<List<Medicine>> getPrescriptionMedicines() {
        List<Medicine> medicines = medicineService.getPrescriptionMedicines();
        return ResponseEntity.ok(medicines);
    }

    @GetMapping("/non-prescription")
    public ResponseEntity<List<Medicine>> getNonPrescriptionMedicines() {
        List<Medicine> medicines = medicineService.getNonPrescriptionMedicines();
        return ResponseEntity.ok(medicines);
    }

    // 删除重复的 searchMedicinesWithStock 方法，只保留一个
    @GetMapping("/search-with-stock")
    public ResponseEntity<Map<String, Object>> searchMedicinesWithStock(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100") int size) {

        System.out.println("=== 接收搜索请求（包含库存） ===");
        System.out.println("keyword: " + keyword);
        System.out.println("category: " + category);
        System.out.println("page: " + page);
        System.out.println("size: " + size);

        try {
            Page<MedicineWithStockDTO> medicinePage = medicineService.searchMedicinesWithStock(keyword, category, page, size);
            List<MedicineWithStockDTO> medicines = medicinePage.getContent();

            System.out.println("搜索结果数量: " + medicines.size());

            // 打印库存信息用于调试
            medicines.forEach(med -> {
                System.out.println("药品: " + med.getGenericName() + ", 库存: " + med.getStockQuantity());
            });

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", medicines);
            response.put("total", medicinePage.getTotalElements());
            response.put("currentPage", page);
            response.put("totalPages", medicinePage.getTotalPages());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("搜索出错: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "搜索失败: " + e.getMessage());
            response.put("data", List.of());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/with-stock")
    public ResponseEntity<Page<MedicineWithStockDTO>> getAllMedicinesWithStock(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MedicineWithStockDTO> medicines = medicineService.getAllMedicinesWithStock(pageable);
        return ResponseEntity.ok(medicines);
    }
}