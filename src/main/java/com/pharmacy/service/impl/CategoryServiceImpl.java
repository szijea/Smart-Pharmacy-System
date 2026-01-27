package com.pharmacy.service.impl;

import com.pharmacy.entity.Category;
import com.pharmacy.repository.CategoryRepository;
import com.pharmacy.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Category findById(Integer id) {
        Optional<Category> category = categoryRepository.findById(id);
        return category.orElse(null);
    }

    @Override
    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public void deleteById(Integer id) {
        categoryRepository.deleteById(id);
    }

    @Override
    public List<Category> findByParentId(Integer parentId) {
        return categoryRepository.findByParentId(parentId);
    }

    @Override
    public Category findByCategoryName(String categoryName) {
        Optional<Category> category = categoryRepository.findByCategoryName(categoryName);
        return category.orElse(null);
    }

    // 在 CategoryServiceImpl.java 中实现新增方法
    @Override
    public List<Category> searchByCategoryName(String categoryName) {
        return categoryRepository.findByCategoryNameContaining(categoryName);
    }

    @Override
    public Category findByCategoryNameIgnoreCase(String categoryName) {
        Optional<Category> category = categoryRepository.findByCategoryNameIgnoreCase(categoryName);
        return category.orElse(null);
    }

    @Override
    public boolean existsByCategoryName(String categoryName) {
        return categoryRepository.findByCategoryName(categoryName).isPresent();
    }

    @Override
    public List<Category> findTopLevelCategories() {
        return categoryRepository.findByParentId(0);
    }
}