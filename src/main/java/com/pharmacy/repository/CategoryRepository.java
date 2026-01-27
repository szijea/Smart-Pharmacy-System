package com.pharmacy.repository;

import com.pharmacy.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    // 根据分类名称查找（精确匹配）
    Optional<Category> findByCategoryName(String categoryName);

    // 查找所有一级分类（parent_id = 0）
    List<Category> findByParentId(Integer parentId);

    // 根据分类名称模糊搜索
    List<Category> findByCategoryNameContaining(String categoryName);

    // 根据分类名称查找（忽略大小写）
    Optional<Category> findByCategoryNameIgnoreCase(String categoryName);

    // 查找所有活跃的分类（如果有状态字段的话）
    // List<Category> findByStatus(Integer status);
}