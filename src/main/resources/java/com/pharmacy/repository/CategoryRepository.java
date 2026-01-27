package com.pharmacy.repository;

import com.pharmacy.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    // 按父分类ID查询
    List<Category> findByParentId(Integer parentId);

    // 按分类名称查询
    List<Category> findByCategoryNameContaining(String categoryName);

    // 按排序字段排序查询
    List<Category> findAllByOrderBySortAsc();
}