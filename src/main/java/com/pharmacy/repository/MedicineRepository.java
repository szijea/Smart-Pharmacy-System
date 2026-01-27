package com.pharmacy.repository;

import com.pharmacy.entity.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, String> {

    // 多字段搜索 - 使用 CONCAT 确保字符集正确
    @Query("SELECT m FROM Medicine m WHERE " +
            "m.genericName LIKE CONCAT('%', :keyword, '%') OR " +
            "m.tradeName LIKE CONCAT('%', :keyword, '%') OR " +
            "m.description LIKE CONCAT('%', :keyword, '%') OR " +
            "m.manufacturer LIKE CONCAT('%', :keyword, '%') OR " +
            "m.spec LIKE CONCAT('%', :keyword, '%') OR " +
            // 新增：支持按条码搜索
            "m.barcode LIKE CONCAT('%', :keyword, '%')")
    List<Medicine> searchByKeyword(@Param("keyword") String keyword);

    // 原生 SQL 搜索 - 简化版本
    @Query(value = "SELECT * FROM medicine WHERE " +
            "generic_name LIKE CONCAT('%', :keyword, '%') OR " +
            "trade_name LIKE CONCAT('%', :keyword, '%') OR " +
            "description LIKE CONCAT('%', :keyword, '%') OR " +
            "manufacturer LIKE CONCAT('%', :keyword, '%') OR " +
            "spec LIKE CONCAT('%', :keyword, '%') OR " +
            // 新增：支持按条码搜索
            "barcode LIKE CONCAT('%', :keyword, '%')",
            nativeQuery = true)
    List<Medicine> searchByKeywordNative(@Param("keyword") String keyword);

    // 根据是否处方药搜索
    List<Medicine> findByIsRx(Boolean isRx);

    // 检查批准文号是否存在
    boolean existsByApprovalNo(String approvalNo);

    // 根据分类ID搜索（分页）
    Page<Medicine> findByCategoryId(Integer categoryId, Pageable pageable);

    // 添加根据分类ID搜索（返回List）
    List<Medicine> findByCategoryId(Integer categoryId);
    // 在 MedicineRepository.java 中添加这些方法

    // 获取药品分类占比
    @Query("SELECT c.categoryName, COUNT(m) FROM Medicine m JOIN Category c ON m.categoryId = c.categoryId GROUP BY c.categoryId, c.categoryName")
    List<Object[]> getCategoryDistribution();

    // 根据ID列表获取药品信息
    List<Medicine> findByMedicineIdIn(List<String> medicineIds);

    // 根据批准文号查找药品（用于创建时去重）
    Medicine findByApprovalNo(String approvalNo);

    // 根据通用名 + 规格 + 厂家查找（用于当 approvalNo 缺失时的去重）
    Medicine findByGenericNameAndSpecAndManufacturer(String genericName, String spec, String manufacturer);

    // 忽略软删除的查询方法
    @Query("SELECT m FROM Medicine m WHERE m.deleted = false")
    Page<Medicine> findAllActive(Pageable pageable);

    @Query("SELECT m FROM Medicine m WHERE m.deleted = false AND m.medicineId IN :ids")
    List<Medicine> findActiveByMedicineIdIn(@Param("ids") List<String> ids);

    @Query("SELECT m FROM Medicine m WHERE m.deleted = false AND (" +
            "m.genericName LIKE CONCAT('%', :keyword, '%') OR " +
            "m.tradeName LIKE CONCAT('%', :keyword, '%') OR " +
            "m.description LIKE CONCAT('%', :keyword, '%') OR " +
            "m.manufacturer LIKE CONCAT('%', :keyword, '%') OR " +
            "m.spec LIKE CONCAT('%', :keyword, '%') OR " +
            // 新增：支持按条码搜索
            "m.barcode LIKE CONCAT('%', :keyword, '%'))")
    List<Medicine> searchActiveByKeyword(@Param("keyword") String keyword);

    // 分类占比：按 category_id 分组计数（兼容无 Category 表的情况）
    @Query(value = "SELECT category_id AS cat, COUNT(1) AS cnt FROM medicine GROUP BY category_id", nativeQuery = true)
    List<Object[]> countGroupByCategory();
}