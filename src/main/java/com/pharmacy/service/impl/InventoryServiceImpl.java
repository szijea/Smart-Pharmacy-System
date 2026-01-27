package com.pharmacy.service.impl;

import com.pharmacy.entity.Inventory;
import com.pharmacy.dto.InventoryDTO;
import com.pharmacy.dto.CurrentStockDTO;
import com.pharmacy.repository.InventoryRepository;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.service.InventoryService;
import com.pharmacy.util.StockStatusUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Override
    public Inventory findById(Long id) {
        try {
            return inventoryRepository.findById(id).orElse(null);
        } catch (Exception e) {
            System.err.println("查询库存失败 findById: " + e.getMessage());
            return null;
        }
    }

    @Override
    @Transactional
    public boolean updateStockForOrder(String medicineId, Integer quantity, String orderId) {
        System.out.println("=== 开始更新库存 ===");
        System.out.println("药品ID: " + medicineId + ", 数量: " + quantity + ", 订单ID: " + orderId);

        try {
            // 1. 获取该药品的所有库存批次
            List<Inventory> inventories = inventoryRepository.findByMedicineId(medicineId);
            System.out.println("找到库存批次数量: " + inventories.size());

            if (inventories.isEmpty()) {
                System.err.println("❌ 没有找到药品 " + medicineId + " 的库存记录");
                return false;
            }

            // 2. 按先进先出原则排序：先按过期日期（近的优先），再按创建时间
            List<Inventory> sortedInventories = inventories.stream()
                    .filter(inv -> inv.getStockQuantity() != null && inv.getStockQuantity() > 0)
                    .sorted(Comparator
                            .comparing(Inventory::getExpiryDate, Comparator.nullsFirst(Comparator.naturalOrder()))
                            .thenComparing(Inventory::getCreateTime))
                    .collect(Collectors.toList());

            int remainingToDeduct = quantity;

            // 3. 逐个批次扣减库存
            for (Inventory inventory : sortedInventories) {
                if (remainingToDeduct <= 0) break;

                int availableInBatch = inventory.getStockQuantity();
                int deductAmount = Math.min(remainingToDeduct, availableInBatch);

                System.out.println("批次 " + inventory.getBatchNo() +
                        " 当前库存: " + availableInBatch +
                        ", 扣减: " + deductAmount);

                // 扣减库存
                inventory.setStockQuantity(availableInBatch - deductAmount);
                inventoryRepository.save(inventory);

                remainingToDeduct -= deductAmount;

                System.out.println("扣减后库存: " + inventory.getStockQuantity() +
                        ", 剩余需扣减: " + remainingToDeduct);
            }

            // 4. 检查是否完全扣减
            if (remainingToDeduct > 0) {
                System.err.println("❌ 库存不足，药品ID: " + medicineId +
                        ", 需求: " + quantity + ", 实际扣减: " + (quantity - remainingToDeduct));
                return false;
            }

            System.out.println("✅ 库存更新成功，药品ID: " + medicineId + ", 扣减数量: " + quantity);
            return true;

        } catch (Exception e) {
            System.err.println("❌ 更新库存失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean checkStock(String medicineId, Integer quantity) {
        Integer currentStock = getCurrentStock(medicineId);
        boolean sufficient = currentStock >= quantity;
        System.out.println("检查库存 - 药品ID: " + medicineId +
                ", 需求: " + quantity + ", 当前库存: " + currentStock +
                ", 是否充足: " + sufficient);
        return sufficient;
    }

    // 只保留一个 getCurrentStock 方法
    @Override
    public Integer getCurrentStock(String medicineId) {
        try {
            System.out.println("=== InventoryService.getCurrentStock 被调用 ===");
            System.out.println("药品ID: '" + medicineId + "'");

            // 方法1: 使用修复后的Repository查询
            Integer totalStock = inventoryRepository.getTotalStockByMedicineId(medicineId);
            System.out.println("Repository查询结果: " + totalStock);

            // 方法2: 备用查询 - 直接使用findByMedicineId手动计算
            List<Inventory> inventories = inventoryRepository.findByMedicineId(medicineId);
            int manualSum = inventories.stream()
                    .mapToInt(inv -> inv.getStockQuantity() == null ? 0 : inv.getStockQuantity())
                    .sum();
            System.out.println("手动计算库存: " + manualSum);
            System.out.println("找到的库存记录数: " + inventories.size());

            inventories.forEach(inv -> {
                System.out.println("批次: " + inv.getBatchNo() + ", 数量: " + inv.getStockQuantity());
            });

            // 优先使用Repository结果，如果为null则使用手动计算
            Integer result = totalStock != null ? totalStock : manualSum;
            System.out.println("最终返回库存: " + result);

            return result;
        } catch (Exception e) {
            System.err.println("查询库存失败: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public List<Inventory> getExpiringSoon() {
        LocalDate today = LocalDate.now();
        LocalDate threeMonthsLater = today.plusMonths(3);
        List<Inventory> expiring = inventoryRepository.findExpiringSoon(today, threeMonthsLater);
        System.out.println("找到即将过期药品数量: " + expiring.size());
        return expiring;
    }

    @Override
    public List<Inventory> findAll() {
        List<Inventory> all = inventoryRepository.findAll();
        System.out.println("所有库存记录数量: " + all.size());
        return all;
    }

    @Override
    public List<Inventory> getLowStock() {
        List<Inventory> lowStock = inventoryRepository.findLowStock();
        System.out.println("低库存药品数量: " + lowStock.size());
        return lowStock;
    }

    @Override
    @Transactional
    public boolean restoreStock(String medicineId, Integer quantity, String refundOrderId) {
        System.out.println("=== 开始恢复库存 ===");
        System.out.println("药品ID: " + medicineId + ", 数量: " + quantity + ", 退单ID: " + refundOrderId);

        try {
            // 1. 获取该药品的所有库存批次
            List<Inventory> inventories = inventoryRepository.findByMedicineId(medicineId);
            System.out.println("找到库存批次数量: " + inventories.size());

            if (inventories.isEmpty()) {
                System.err.println("❌ 没有找到药品 " + medicineId + " 的库存记录");
                return false;
            }

            // 2. 按先进先出原则排序：优先选择过期日期较近的批次
            List<Inventory> sortedInventories = inventories.stream()
                    .sorted(Comparator
                            .comparing(Inventory::getExpiryDate, Comparator.nullsFirst(Comparator.naturalOrder()))
                            .thenComparing(Inventory::getCreateTime))
                    .collect(Collectors.toList());

            int remainingToRestore = quantity;

            // 3. 逐个批次恢复库存
            for (Inventory inventory : sortedInventories) {
                if (remainingToRestore <= 0) break;

                // 恢复库存到当前批次
                int currentStock = inventory.getStockQuantity() == null ? 0 : inventory.getStockQuantity();
                inventory.setStockQuantity(currentStock + remainingToRestore);
                inventoryRepository.save(inventory);

                System.out.println("批次 " + inventory.getBatchNo() +
                        " 原库存: " + currentStock +
                        ", 恢复后: " + inventory.getStockQuantity());

                remainingToRestore = 0; // 由于我们直接恢复所有数量到一个批次，所以剩余为0

                System.out.println("恢复后库存: " + inventory.getStockQuantity());
            }

            // 4. 记录库存变动（如果需要）
            // 这里可以调用库存变动记录服务
            System.out.println("✅ 库存恢复成功，药品ID: " + medicineId + ", 恢复数量: " + quantity);
            return true;

        } catch (Exception e) {
            System.err.println("❌ 恢复库存失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<InventoryDTO> findDTOByMedicineId(String medicineId) {
        return inventoryRepository.findDTOByMedicineId(medicineId);
    }

    @Override
    public Inventory save(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    @Override
    public void deleteById(Long id) {
        inventoryRepository.deleteById(id);
    }

    @Override
    public List<InventoryDTO> findAllWithMedicineDTO() {
        try {
            List<InventoryDTO> list = inventoryRepository.findAllWithMedicine();
            // 填充状态字段
            for (InventoryDTO dto : list) {
                String stockStatus = StockStatusUtil.calcStockStatus(dto.getStockQuantity(), dto.getMinStock());
                String expiryStatus = StockStatusUtil.calcExpiryStatus(dto.getExpiryDate());
                dto.setStockStatus(stockStatus);
                dto.setExpiryStatus(expiryStatus);
                dto.setSafetyStock(dto.getMinStock());
                dto.setEarliestExpiryDate(dto.getExpiryDate()); // 单批次默认等于自身
                // 新增字段: 分类与是否处方药
                if (dto.getMedicineId() != null) {
                    inventoryRepository.findByMedicineId(dto.getMedicineId()).stream().findFirst().ifPresent(inv -> {
                        if (inv.getMedicine() != null) {
                            dto.setMedicineCategoryId(inv.getMedicine().getCategoryId());
                            dto.setMedicineIsRx(inv.getMedicine().getIsRx());
                        }
                    });
                }
            }
            System.out.println("返回 InventoryDTO 数量: " + list.size());
            return list;
        } catch (Exception e) {
            System.err.println("查询 InventoryDTO 失败: " + e.getMessage());
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    @Override
    public InventoryDTO findDTOById(Long inventoryId) {
        try {
            return inventoryRepository.findDTOById(inventoryId);
        } catch (Exception e) {
            System.err.println("查询 InventoryDTO 详情失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public java.util.List<com.pharmacy.dto.InventoryDTO> findDTOByBatchNo(String batchNo) {
        try {
            java.util.List<com.pharmacy.dto.InventoryDTO> list = inventoryRepository.findDTOByBatchNo(batchNo);
            java.time.LocalDate earliest = null;
            for (com.pharmacy.dto.InventoryDTO dto : list) {
                String stockStatus = com.pharmacy.util.StockStatusUtil.calcStockStatus(dto.getStockQuantity(), dto.getMinStock());
                String expiryStatus = com.pharmacy.util.StockStatusUtil.calcExpiryStatus(dto.getExpiryDate());
                dto.setStockStatus(stockStatus);
                dto.setExpiryStatus(expiryStatus);
                dto.setSafetyStock(dto.getMinStock());
                earliest = com.pharmacy.util.StockStatusUtil.mergeEarliest(earliest, dto.getExpiryDate());
                if (dto.getMedicineId() != null) {
                    inventoryRepository.findByMedicineId(dto.getMedicineId()).stream().findFirst().ifPresent(inv -> {
                        if (inv.getMedicine() != null) {
                            dto.setMedicineCategoryId(inv.getMedicine().getCategoryId());
                            dto.setMedicineIsRx(inv.getMedicine().getIsRx());
                        }
                    });
                }
            }
            for (com.pharmacy.dto.InventoryDTO dto : list) {
                dto.setEarliestExpiryDate(earliest);
            }
            return list;
        } catch (Exception e) {
            System.err.println("按批号查询库存失败: " + e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    @Override
    @Transactional
    public Inventory createBatch(String medicineId, String batchNo, Integer stockQuantity, Integer minStock, Integer maxStock, java.math.BigDecimal purchasePrice, java.time.LocalDate expiryDate, String supplier) {
        Inventory inv = new Inventory();
        inv.setMedicineId(medicineId);
        inv.setBatchNo(batchNo);
        inv.setStockQuantity(stockQuantity != null ? stockQuantity : 0);
        inv.setMinStock(minStock);
        inv.setMaxStock(maxStock);
        inv.setPurchasePrice(purchasePrice);
        inv.setExpiryDate(expiryDate);
        inv.setSupplier(supplier);
        return inventoryRepository.save(inv);
    }

    @Override
    @Transactional
    public Inventory replenish(String medicineId, Integer addQuantity, String preferredBatchNo, Integer minStock) {
        if (addQuantity == null || addQuantity < 0) return null; // 仅负数非法，0允许
        Inventory target = null;
        if (preferredBatchNo != null && !preferredBatchNo.isBlank()) {
            List<Inventory> byBatch = inventoryRepository.findByBatchNo(preferredBatchNo);
            target = byBatch.stream().filter(i -> medicineId.equals(i.getMedicineId())).findFirst().orElse(null);
        }
        if (target == null) {
            List<Inventory> list = inventoryRepository.findByMedicineId(medicineId);
            target = list.stream()
                    .sorted(Comparator.comparing(Inventory::getStockQuantity).thenComparing(Inventory::getCreateTime))
                    .findFirst().orElse(null);
        }
        if (target == null) {
            String newBatch = preferredBatchNo != null && !preferredBatchNo.isBlank() ? preferredBatchNo : ("AUTO" + System.currentTimeMillis());
            return createBatch(medicineId, newBatch, addQuantity, minStock, null, null, null, null);
        }
        if (addQuantity > 0) {
            target.setStockQuantity(target.getStockQuantity() + addQuantity);
        }
        if (minStock != null) {
            target.setMinStock(minStock);
        }
        return inventoryRepository.save(target);
    }

    @Override
    @Transactional
    public Inventory replenish(String medicineId, Integer addQuantity, String preferredBatchNo) {
        return replenish(medicineId, addQuantity, preferredBatchNo, null);
    }

    @Override
    public List<CurrentStockDTO> getCurrentStocks(List<String> medicineIds) {
        if (medicineIds == null || medicineIds.isEmpty()) return java.util.Collections.emptyList();
        // 读取全部库存聚合一次减少 N+1
        List<Object[]> aggregates = inventoryRepository.getCurrentStockByMedicine();
        java.util.Map<String,Integer> stockMap = new java.util.HashMap<>();
        for(Object[] row: aggregates){
            if(row!=null && row.length>=2){
                String mid = String.valueOf(row[0]);
                Integer qty = row[1]==null?0:Integer.valueOf(String.valueOf(row[1]));
                stockMap.put(mid, qty);
            }
        }
        List<com.pharmacy.entity.Medicine> meds = medicineRepository.findAllById(medicineIds);
        java.util.Map<String, com.pharmacy.entity.Medicine> medMap = new java.util.HashMap<>();
        for(com.pharmacy.entity.Medicine m : meds){ medMap.put(String.valueOf(m.getMedicineId()), m); }
        List<CurrentStockDTO> result = new java.util.ArrayList<>();
        for(String id : medicineIds){
            com.pharmacy.entity.Medicine m = medMap.get(String.valueOf(id));
            Integer qty = stockMap.getOrDefault(String.valueOf(id),0);
            CurrentStockDTO dto = new CurrentStockDTO(String.valueOf(id), m!=null?m.getGenericName():null, m!=null?m.getTradeName():null, qty);
            result.add(dto);
        }
        return result;
    }

    @Override
    public Page<InventoryDTO> pageCurrentStocks(int page, int size) {
        if(page < 0) page = 0; if(size <= 0) size = 10;
        var pageable = PageRequest.of(page, size);
        return inventoryRepository.findAllWithMedicine(pageable);
    }

    @Override
    @Transactional
    public boolean deductStockForTransfer(String medicineId, String batchNo, int quantity) {
        List<Inventory> inventories = inventoryRepository.findByMedicineIdAndBatchNo(medicineId, batchNo);
        if (inventories.isEmpty()) {
            return false; // Or throw an exception
        }
        Inventory inventory = inventories.get(0);
        if (inventory.getStockQuantity() < quantity) {
            return false; // Insufficient stock
        }
        inventory.setStockQuantity(inventory.getStockQuantity() - quantity);
        inventoryRepository.save(inventory);
        return true;
    }

    @Override
    @Transactional
    public void addStockForTransfer(String medicineId, String batchNo, int quantity) {
        List<Inventory> inventories = inventoryRepository.findByMedicineIdAndBatchNo(medicineId, batchNo);
        Inventory inventory;
        if (inventories.isEmpty()) {
            // If the destination does not have this batch, create a new inventory record
            inventory = new Inventory();
            inventory.setMedicineId(medicineId);
            inventory.setBatchNo(batchNo);
            inventory.setStockQuantity(quantity);
            // Set other fields to default values if necessary
        } else {
            inventory = inventories.get(0);
            inventory.setStockQuantity(inventory.getStockQuantity() + quantity);
        }
        inventoryRepository.save(inventory);
    }
}
