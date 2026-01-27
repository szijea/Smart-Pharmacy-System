// 在 CategoryService.java 中添加这些方法
package com.pharmacy.service;

import com.pharmacy.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> findAll();
    Category findById(Integer id);
    Category save(Category category);
    void deleteById(Integer id);
    List<Category> findByParentId(Integer parentId);
    Category findByCategoryName(String categoryName);

    // 新增方法
    List<Category> searchByCategoryName(String categoryName);
    Category findByCategoryNameIgnoreCase(String categoryName);
    boolean existsByCategoryName(String categoryName);
    List<Category> findTopLevelCategories(); // 查找一级分类
}