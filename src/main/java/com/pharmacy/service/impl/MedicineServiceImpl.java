// MedicineServiceImpl.java - 修复版本
package com.pharmacy.service.impl;

import com.pharmacy.dto.MedicineWithStockDTO;
import com.pharmacy.entity.Medicine;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.service.MedicineService;
import com.pharmacy.service.InventoryService;
import com.pharmacy.util.StockStatusUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MedicineServiceImpl implements MedicineService {

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private InventoryService inventoryService;

    // 基本 CRUD 方法
    @Override
    public Page<Medicine> findAll(Pageable pageable) {
        return medicineRepository.findAll(pageable);
    }

    @Override
    public Optional<Medicine> findById(String medicineId) {
        return medicineRepository.findById(medicineId);
    }

    @Override
    public Medicine save(Medicine medicine) {
        return medicineRepository.save(medicine);
    }

    @Override
    public void deleteById(String id) {
        // delegate to repository (repository deleteById expects String)
        medicineRepository.deleteById(id);
    }

    // 搜索方法
    @Override
    public List<Medicine> searchMedicines(String keyword) {
        return medicineRepository.searchByKeyword(keyword);
    }

    @Override
    public Page<Medicine> searchMedicines(String keyword, Pageable pageable) {
        // 简化实现，实际应该使用分页查询
        List<Medicine> medicines = medicineRepository.searchByKeyword(keyword);
        return new PageImpl<>(medicines, pageable, medicines.size());
    }

    // 新增：分页搜索方法（包含分类）
    @Override
    public Page<Medicine> searchMedicines(String keyword, String category, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        if (keyword != null && !keyword.trim().isEmpty()) {
            List<Medicine> medicines = medicineRepository.searchActiveByKeyword(keyword);

            // 如果指定了分类，进行过滤
            if (category != null && !category.trim().isEmpty()) {
                medicines = medicines.stream()
                        .filter(medicine -> {
                            // 这里需要根据您的分类逻辑进行过滤
                            // 暂时返回所有，您可以根据需要实现具体逻辑
                            return true;
                        })
                        .collect(Collectors.toList());
            }

            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), medicines.size());
            return new PageImpl<>(medicines.subList(start, end), pageable, medicines.size());
        } else {
            // 如果没有关键词，返回所有药品
            return medicineRepository.findAllActive(pageable);
        }
    }

    // 分类相关方法
    @Override
    public Page<Medicine> findByCategory(String category, Pageable pageable) {
        // 简化实现，您需要根据实际分类逻辑来实现
        return medicineRepository.findAll(pageable);
    }

    @Override
    public List<Medicine> getMedicinesByCategory(String category) {
        // 简化实现，您需要根据实际分类逻辑来实现
        return medicineRepository.findAll();
    }

    // 获取所有药品
    @Override
    public Page<Medicine> getAllMedicines(Pageable pageable) {
        return medicineRepository.findAll(pageable);
    }

    // 处方药相关
    @Override
    public List<Medicine> getPrescriptionMedicines() {
        return medicineRepository.findByIsRx(true);
    }

    @Override
    public List<Medicine> getNonPrescriptionMedicines() {
        return medicineRepository.findByIsRx(false);
    }

    // 包含库存的搜索方法 - 修复：确保只有一个定义
    @Override
    public Page<MedicineWithStockDTO> searchMedicinesWithStock(String keyword, String category, int page, int size) {
        System.out.println("=== 开始搜索药品（包含库存） ===");
        System.out.println("搜索参数 - keyword: " + keyword + ", category: " + category + ", page: " + page + ", size: " + size);

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Medicine> medicinePage = searchMedicines(keyword, category, page, size);

        System.out.println("找到药品数量: " + medicinePage.getNumberOfElements());

        List<MedicineWithStockDTO> dtos = medicinePage.getContent().stream()
                .map(medicine -> {
                    MedicineWithStockDTO dto = new MedicineWithStockDTO();
                    // medicineId is String in entity and DTO
                    dto.setMedicineId(medicine.getMedicineId());
                    dto.setGenericName(medicine.getGenericName());
                    dto.setTradeName(medicine.getTradeName());
                    dto.setSpec(medicine.getSpec());
                    dto.setManufacturer(medicine.getManufacturer());
                    dto.setApprovalNo(medicine.getApprovalNo());
                    dto.setBarcode(medicine.getBarcode());
                    dto.setProductionDate(medicine.getProductionDate());
                    dto.setExpiryDate(medicine.getExpiryDate());
                    dto.setStatus(medicine.getStatus());
                    dto.setRetailPrice(medicine.getRetailPrice());
                    dto.setMemberPrice(medicine.getMemberPrice());
                    dto.setIsRx(medicine.getIsRx());
                    dto.setUnit(medicine.getUnit());
                    dto.setDescription(medicine.getDescription());
                    dto.setUsageDosage(medicine.getUsageDosage());
                    dto.setContraindication(medicine.getContraindication());

                    // 获取库存信息 - 添加详细调试
                    System.out.println("正在查询药品 " + medicine.getMedicineId() + " 的库存...");
                    Integer stockQuantity = inventoryService.getCurrentStock(medicine.getMedicineId());
                    System.out.println("药品 " + medicine.getMedicineId() + " 的库存结果: " + stockQuantity);

                    dto.setStockQuantity(stockQuantity);

                    // 新增：批号与最早效期
                    java.util.List<com.pharmacy.dto.InventoryDTO> invList = inventoryService.findDTOByMedicineId(medicine.getMedicineId());
                    java.util.List<String> batchNos = invList.stream().map(com.pharmacy.dto.InventoryDTO::getBatchNo).filter(java.util.Objects::nonNull).distinct().toList();
                    dto.setBatchNos(batchNos);
                    java.time.LocalDate earliest = invList.stream().map(com.pharmacy.dto.InventoryDTO::getExpiryDate)
                            .filter(d -> d != null && !d.isBefore(java.time.LocalDate.now()))
                            .sorted().findFirst().orElse(null);
                    dto.setEarliestExpiryDate(earliest);

                    // 计算库存状态与效期状态
                    Integer safeBase = invList.stream().map(com.pharmacy.dto.InventoryDTO::getMinStock).filter(ms->ms!=null && ms>0).sorted().findFirst().orElse(1);
                    String stockStatus = StockStatusUtil.calcStockStatus(stockQuantity, safeBase);
                    dto.setStockStatus(stockStatus);
                    String expiryStatus = StockStatusUtil.calcExpiryStatus(medicine.getExpiryDate());
                    dto.setExpiryStatus(expiryStatus);

                    return dto;
                })
                .collect(Collectors.toList());

        System.out.println("=== 搜索完成，返回 " + dtos.size() + " 个结果 ===");
        return new PageImpl<>(dtos, pageable, medicinePage.getTotalElements());
    }

    // 修复：添加缺失的 getAllMedicinesWithStock 方法实现
    @Override
    public Page<MedicineWithStockDTO> getAllMedicinesWithStock(Pageable pageable) {
        System.out.println("=== 获取所有药品（包含库存） ===");

        Page<Medicine> medicinePage = medicineRepository.findAll(pageable);

        System.out.println("找到药品数量: " + medicinePage.getNumberOfElements());

        List<MedicineWithStockDTO> dtos = medicinePage.getContent().stream()
                .map(medicine -> {
                    MedicineWithStockDTO dto = new MedicineWithStockDTO();
                    dto.setMedicineId(medicine.getMedicineId());
                    dto.setGenericName(medicine.getGenericName());
                    dto.setTradeName(medicine.getTradeName());
                    dto.setSpec(medicine.getSpec());
                    dto.setManufacturer(medicine.getManufacturer());
                    dto.setApprovalNo(medicine.getApprovalNo());
                    dto.setBarcode(medicine.getBarcode());
                    dto.setProductionDate(medicine.getProductionDate());
                    dto.setExpiryDate(medicine.getExpiryDate());
                    dto.setStatus(medicine.getStatus());
                    dto.setRetailPrice(medicine.getRetailPrice());
                    dto.setMemberPrice(medicine.getMemberPrice());
                    dto.setIsRx(medicine.getIsRx());
                    dto.setUnit(medicine.getUnit());
                    dto.setDescription(medicine.getDescription());
                    dto.setUsageDosage(medicine.getUsageDosage());
                    dto.setContraindication(medicine.getContraindication());

                    // 获取库存信息
                    System.out.println("正在查询药品 " + medicine.getMedicineId() + " 的库存...");
                    Integer stockQuantity = inventoryService.getCurrentStock(medicine.getMedicineId());
                    System.out.println("药品 " + medicine.getMedicineId() + " 的库存结果: " + stockQuantity);

                    dto.setStockQuantity(stockQuantity);

                    java.util.List<com.pharmacy.dto.InventoryDTO> invList = inventoryService.findDTOByMedicineId(medicine.getMedicineId());
                    java.util.List<String> batchNos = invList.stream().map(com.pharmacy.dto.InventoryDTO::getBatchNo).filter(java.util.Objects::nonNull).distinct().toList();
                    dto.setBatchNos(batchNos);
                    java.time.LocalDate earliest = invList.stream().map(com.pharmacy.dto.InventoryDTO::getExpiryDate)
                            .filter(d -> d != null && !d.isBefore(java.time.LocalDate.now()))
                            .sorted().findFirst().orElse(null);
                    dto.setEarliestExpiryDate(earliest);

                    // 计算库存与效期状态
                    Integer safeBase2 = invList.stream().map(com.pharmacy.dto.InventoryDTO::getMinStock).filter(ms->ms!=null && ms>0).sorted().findFirst().orElse(1);
                    String stockStatus2 = StockStatusUtil.calcStockStatus(stockQuantity, safeBase2);
                    dto.setStockStatus(stockStatus2);
                    String expiryStatus2 = StockStatusUtil.calcExpiryStatus(medicine.getExpiryDate());
                    dto.setExpiryStatus(expiryStatus2);

                    return dto;
                })
                .collect(Collectors.toList());

        System.out.println("=== 获取完成，返回 " + dtos.size() + " 个结果 ===");
        return new PageImpl<>(dtos, pageable, medicinePage.getTotalElements());
    }

    // 其他业务方法
    @Override
    public Medicine getMedicineById(String id) {
        return medicineRepository.findById(id).orElse(null);
    }

    @Override
    public Medicine createMedicine(Medicine medicine) {
        return medicineRepository.save(medicine);
    }

    @Override
    public Medicine findByApprovalNo(String approvalNo) {
        if (approvalNo == null) return null;
        return medicineRepository.findByApprovalNo(approvalNo);
    }

    @Override
    public Medicine findByGenericSpecManufacturer(String genericName, String spec, String manufacturer) {
        if (genericName == null) genericName = "";
        if (spec == null) spec = "";
        if (manufacturer == null) manufacturer = "";
        return medicineRepository.findByGenericNameAndSpecAndManufacturer(genericName, spec, manufacturer);
    }

    @Override
    public Medicine updateMedicine(String id, Medicine medicine) {
        Medicine existing = medicineRepository.findById(id).orElse(null);
        if (existing == null) {
            medicine.setMedicineId(id);
            medicine.setDeleted(false);
            return medicineRepository.save(medicine);
        }
        if(Boolean.TRUE.equals(existing.getDeleted())) {
            // 不允许更新已软删除的记录
            return existing;
        }
        // 仅覆盖非空字段
        if (medicine.getGenericName() != null) existing.setGenericName(medicine.getGenericName());
        if (medicine.getTradeName() != null) existing.setTradeName(medicine.getTradeName());
        if (medicine.getSpec() != null) existing.setSpec(medicine.getSpec());
        if (medicine.getApprovalNo() != null) existing.setApprovalNo(medicine.getApprovalNo());
        if (medicine.getCategoryId() != null) existing.setCategoryId(medicine.getCategoryId());
        if (medicine.getManufacturer() != null) existing.setManufacturer(medicine.getManufacturer());
        if (medicine.getRetailPrice() != null) existing.setRetailPrice(medicine.getRetailPrice());
        if (medicine.getMemberPrice() != null) existing.setMemberPrice(medicine.getMemberPrice());
        if (medicine.getIsRx() != null) existing.setIsRx(medicine.getIsRx());
        if (medicine.getUnit() != null) existing.setUnit(medicine.getUnit());
        if (medicine.getDescription() != null) existing.setDescription(medicine.getDescription());
        if (medicine.getBarcode() != null) existing.setBarcode(medicine.getBarcode());
        if (medicine.getProductionDate() != null) existing.setProductionDate(medicine.getProductionDate());
        if (medicine.getExpiryDate() != null) existing.setExpiryDate(medicine.getExpiryDate());
        if (medicine.getStatus() != null) existing.setStatus(medicine.getStatus());
        return medicineRepository.save(existing);
    }

    @Override
    public void deleteMedicine(String id) {
        Medicine existing = medicineRepository.findById(id).orElse(null);
        if (existing != null) {
            existing.setDeleted(true);
            existing.setStatus("INACTIVE");
            medicineRepository.save(existing);
        }
    }

    @Override
    public void restoreMedicine(String id) {
        Medicine existing = medicineRepository.findById(id).orElse(null);
        if (existing != null && Boolean.TRUE.equals(existing.getDeleted())) {
            existing.setDeleted(false);
            if (existing.getStatus() == null || existing.getStatus().equalsIgnoreCase("INACTIVE")) {
                existing.setStatus("ACTIVE");
            }
            medicineRepository.save(existing);
        }
    }
}
