package com.pharmacy.service.impl;

import com.pharmacy.entity.Inventory;
import com.pharmacy.repository.InventoryRepository;
import com.pharmacy.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
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
                    .filter(inv -> inv.getStockQuantity() > 0) // 使用 getStockQuantity()
                    .sorted(Comparator
                            .comparing(Inventory::getExpiryDate, Comparator.nullsFirst(Comparator.naturalOrder()))
                            .thenComparing(Inventory::getCreateTime))
                    .collect(Collectors.toList());

            int remainingToDeduct = quantity;

            // 3. 逐个批次扣减库存
            for (Inventory inventory : sortedInventories) {
                if (remainingToDeduct <= 0) break;

                int availableInBatch = inventory.getStockQuantity(); // 使用 getStockQuantity()
                int deductAmount = Math.min(remainingToDeduct, availableInBatch);

                System.out.println("批次 " + inventory.getBatchNo() +
                        " 当前库存: " + availableInBatch +
                        ", 扣减: " + deductAmount);

                // 扣减库存
                inventory.setStockQuantity(availableInBatch - deductAmount); // 使用 setStockQuantity()
                inventoryRepository.save(inventory);

                remainingToDeduct -= deductAmount;

                System.out.println("扣减后库存: " + inventory.getStockQuantity() + // 使用 getStockQuantity()
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
                    .mapToInt(Inventory::getStockQuantity)  // 使用 getStockQuantity()
                    .sum();
            System.out.println("手动计算库存: " + manualSum);
            System.out.println("找到的库存记录数: " + inventories.size());

            inventories.forEach(inv -> {
                System.out.println("批次: " + inv.getBatchNo() + ", 数量: " + inv.getStockQuantity()); // 使用 getStockQuantity()
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
    public Inventory findById(Long id) {
        return inventoryRepository.findById(id).orElse(null);
    }

    @Override
    public Inventory save(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    @Override
    public void deleteById(Long id) {
        inventoryRepository.deleteById(id);
    }
}