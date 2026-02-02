package com.pharmacy.repository;

import com.pharmacy.entity.Inventory;
import com.pharmacy.dto.InventoryDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    // 根据药品ID查找库存
    List<Inventory> findByMedicineId(String medicineId);

    // 查找低库存（库存量 <= 最小库存）- 修复：使用 stockQuantity
    @Query("SELECT i FROM Inventory i WHERE i.stockQuantity <= i.minStock")
    List<Inventory> findLowStock();

    // 查找即将过期的药品（3个月内过期）
    @Query("SELECT i FROM Inventory i WHERE i.expiryDate BETWEEN :today AND :threeMonthsLater")
    List<Inventory> findExpiringSoon(@Param("today") LocalDate today,
                                     @Param("threeMonthsLater") LocalDate threeMonthsLater);

    // 查找已过期的药品
    @Query("SELECT i FROM Inventory i WHERE i.expiryDate < :today")
    List<Inventory> findExpired(@Param("today") LocalDate today);

    // 根据批号查找
    List<Inventory> findByBatchNo(String batchNo);

    List<Inventory> findByMedicineIdAndBatchNo(String medicineId, String batchNo);

    @Query("SELECT SUM(i.stockQuantity) FROM Inventory i")
    Long findTotalStockQuantity();

    // 修复：返回当前每个药品的库存汇总（medicine_id, total_stock）
    @Query(value = "SELECT medicine_id, COALESCE(SUM(stock_quantity), 0) FROM inventory GROUP BY medicine_id", nativeQuery = true)
    List<Object[]> getCurrentStockByMedicine();

    // 获取低库存数量（stock_quantity <= min_stock）
    @Query(value = "SELECT COUNT(*) FROM inventory WHERE stock_quantity <= min_stock", nativeQuery = true)
    Integer getLowStockCount();

    // 修复：使用正确的库存列 stock_quantity，medicine_id 为 String
    @Query(value = "SELECT COALESCE(SUM(stock_quantity), 0) FROM inventory WHERE medicine_id = :medicineId", nativeQuery = true)
    Integer getTotalStockByMedicineId(@Param("medicineId") String medicineId);

    // 新增：使用构造函数投影返回 InventoryDTO，避免在 Controller 处发生懒加载问题
    @Query("select new com.pharmacy.dto.InventoryDTO(" +
           "i.id, i.batchNo, i.createTime, i.expiryDate, i.maxStock, " +
           "i.medicineId, i.minStock, i.purchasePrice, i.stockQuantity, i.supplier, i.updateTime, " +
           "m.genericName, m.tradeName, m.spec, m.retailPrice, " +
           "null, null, null, i.minStock, m.categoryId, m.isRx, m.barcode, m.manufacturer) " +
           "from Inventory i join i.medicine m")
    List<com.pharmacy.dto.InventoryDTO> findAllWithMedicine();

    // 新增：分页查询库存（带药品信息）
    @Query("select new com.pharmacy.dto.InventoryDTO(" +
           "i.id, i.batchNo, i.createTime, i.expiryDate, i.maxStock, " +
           "i.medicineId, i.minStock, i.purchasePrice, i.stockQuantity, i.supplier, i.updateTime, " +
           "m.genericName, m.tradeName, m.spec, m.retailPrice, " +
           "null, null, null, i.minStock, m.categoryId, m.isRx, m.barcode, m.manufacturer) " +
           "from Inventory i join i.medicine m")
    org.springframework.data.domain.Page<com.pharmacy.dto.InventoryDTO> findAllWithMedicine(org.springframework.data.domain.Pageable pageable);

    // 新增：根据库存ID返回 InventoryDTO 详情
    @Query("select new com.pharmacy.dto.InventoryDTO(" +
           "i.id, i.batchNo, i.createTime, i.expiryDate, i.maxStock, " +
           "i.medicineId, i.minStock, i.purchasePrice, i.stockQuantity, i.supplier, i.updateTime, " +
           "m.genericName, m.tradeName, m.spec, m.retailPrice, " +
           "null, null, null, i.minStock, m.categoryId, m.isRx, m.barcode, m.manufacturer) " +
           "from Inventory i join i.medicine m where i.id = :inventoryId")
    InventoryDTO findDTOById(@Param("inventoryId") Long inventoryId);

    // 新增：根据药品ID返回该药品所有批次的 InventoryDTO 列表
    @Query("select new com.pharmacy.dto.InventoryDTO(" +
           "i.id, i.batchNo, i.createTime, i.expiryDate, i.maxStock, " +
           "i.medicineId, i.minStock, i.purchasePrice, i.stockQuantity, i.supplier, i.updateTime, " +
           "m.genericName, m.tradeName, m.spec, m.retailPrice, " +
           "null, null, null, i.minStock, m.categoryId, m.isRx, m.barcode, m.manufacturer) " +
           "from Inventory i join i.medicine m where i.medicineId = :medicineId")
    List<InventoryDTO> findDTOByMedicineId(@Param("medicineId") String medicineId);

    // 新增：使用原生查询获取每个药品最早未过期的有效期日期（仅取 >= 当前日期）
    @Query(value = "SELECT medicine_id, MIN(expiry_date) FROM inventory WHERE expiry_date >= CURRENT_DATE GROUP BY medicine_id", nativeQuery = true)
    List<Object[]> getEarliestNonExpiredExpiryByMedicine();

    // 新增：根据批号返回 InventoryDTO 列表
    @Query("select new com.pharmacy.dto.InventoryDTO(" +
           "i.id, i.batchNo, i.createTime, i.expiryDate, i.maxStock, " +
           "i.medicineId, i.minStock, i.purchasePrice, i.stockQuantity, i.supplier, i.updateTime, " +
           "m.genericName, m.tradeName, m.spec, m.retailPrice, " +
           "null, null, null, i.minStock, m.categoryId, m.isRx, m.barcode, m.manufacturer) " +
           "from Inventory i join i.medicine m where i.batchNo = :batchNo")
    List<com.pharmacy.dto.InventoryDTO> findDTOByBatchNo(@Param("batchNo") String batchNo);

    // 自适应低库存：min_stock 有值则用其判断；无值则使用默认阈值(:defaultThreshold)
    @Query(value = "SELECT * FROM inventory i WHERE ( (i.min_stock IS NOT NULL AND i.min_stock > 0 AND i.stock_quantity <= i.min_stock) OR (i.min_stock IS NULL AND i.stock_quantity <= :defaultThreshold) )", nativeQuery = true)
    List<Inventory> findLowStockAdaptive(@Param("defaultThreshold") int defaultThreshold);
}
