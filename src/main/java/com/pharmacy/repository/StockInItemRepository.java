package com.pharmacy.repository;

import com.pharmacy.entity.StockInItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockInItemRepository extends JpaRepository<StockInItem, Long> {

    // 根据入库单ID查找所有项
    List<StockInItem> findByStockInStockInId(Long stockInId);

    // 根据药品ID查找入库记录（直接使用 medicineId 字段）
    List<StockInItem> findByMedicineId(String medicineId);

    // 统计某个药品的总入库数量（使用 medicineId 字段，避免关联路径解析问题）
    @Query("SELECT SUM(s.quantity) FROM StockInItem s WHERE s.medicineId = :medicineId")
    Integer sumQuantityByMedicineId(@Param("medicineId") String medicineId);

    // 根据批号查找
    List<StockInItem> findByBatchNumber(String batchNumber);

    // 删除指定入库单的所有项
    void deleteByStockInStockInId(Long stockInId);
}