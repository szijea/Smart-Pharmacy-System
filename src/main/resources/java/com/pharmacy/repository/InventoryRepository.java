package com.pharmacy.repository;

import com.pharmacy.entity.Inventory;
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

    // 修复：使用正确的库存列 stock_quantity
    @Query(value = "SELECT COALESCE(SUM(stock_quantity), 0) FROM inventory WHERE medicine_id = :medicineId", nativeQuery = true)
    Integer getTotalStockByMedicineId(@Param("medicineId") String medicineId);
}