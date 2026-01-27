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
            "m.spec LIKE CONCAT('%', :keyword, '%')")
    List<Medicine> searchByKeyword(@Param("keyword") String keyword);

    // 原生 SQL 搜索 - 简化版本
    @Query(value = "SELECT * FROM medicine WHERE " +
            "generic_name LIKE CONCAT('%', :keyword, '%') OR " +
            "trade_name LIKE CONCAT('%', :keyword, '%') OR " +
            "description LIKE CONCAT('%', :keyword, '%') OR " +
            "manufacturer LIKE CONCAT('%', :keyword, '%') OR " +
            "spec LIKE CONCAT('%', :keyword, '%')",
            nativeQuery = true)
    List<Medicine> searchByKeywordNative(@Param("keyword") String keyword);

    // 根据分类ID搜索（删除不存在的 findByCategoryName 方法）
    // List<Medicine> findByCategoryName(String categoryName); // 删除这行

    // 根据是否处方药搜索
    List<Medicine> findByIsRx(Boolean isRx);

    // 检查批准文号是否存在
    boolean existsByApprovalNo(String approvalNo);

    // 根据分类ID搜索（分页）
    Page<Medicine> findByCategoryId(Integer categoryId, Pageable pageable);

    // 添加根据分类ID搜索（返回List）
    List<Medicine> findByCategoryId(Integer categoryId);
}