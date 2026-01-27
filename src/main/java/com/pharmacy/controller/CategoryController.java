package com.pharmacy.controller;

import com.pharmacy.entity.Category;
import com.pharmacy.service.CategoryService;
import com.pharmacy.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
// 使用全局 CORS 配置，移除局部 @CrossOrigin
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取所有分类
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCategories() {
        try {
            System.out.println("=== 获取所有分类 ===");

            List<Category> categories = categoryService.findAll();
            System.out.println("从数据库获取的分类数量: " + categories.size());

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", categories);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("获取分类失败: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取分类失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 数据库连接测试
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testConnection() {
        try {
            List<Category> categories = categoryService.findAll();
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "数据库连接正常");
            response.put("categoryCount", categories.size());
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "数据库连接失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 根据ID获取分类
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCategoryById(@PathVariable Integer id) {
        try {
            System.out.println("=== 获取分类详情 ===");
            System.out.println("分类ID: " + id);

            Category category = categoryService.findById(id);
            if (category == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 404);
                response.put("message", "分类不存在");
                return ResponseEntity.status(404).body(response);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", category);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("获取分类详情失败: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取分类详情失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 创建分类
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createCategory(@RequestBody Category category) {
        try {
            System.out.println("=== 创建分类 ===");
            System.out.println("分类名称: " + category.getCategoryName());

            // 检查分类名称是否已存在
            Category existingCategory = categoryService.findByCategoryName(category.getCategoryName());
            if (existingCategory != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 400);
                response.put("message", "分类名称已存在");
                return ResponseEntity.badRequest().body(response);
            }

            Category savedCategory = categoryService.save(category);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 201);
            response.put("message", "分类创建成功");
            response.put("data", savedCategory);

            return ResponseEntity.status(201).body(response);
        } catch (Exception e) {
            System.err.println("创建分类失败: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "创建分类失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCategory(@PathVariable Integer id, @RequestBody Category category) {
        try {
            System.out.println("=== 更新分类 ===");
            System.out.println("分类ID: " + id);

            // 检查分类是否存在
            Category existingCategory = categoryService.findById(id);
            if (existingCategory == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 404);
                response.put("message", "分类不存在");
                return ResponseEntity.status(404).body(response);
            }

            // 检查名称是否与其他分类重复
            Category sameNameCategory = categoryService.findByCategoryName(category.getCategoryName());
            if (sameNameCategory != null && !sameNameCategory.getCategoryId().equals(id)) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 400);
                response.put("message", "分类名称已存在");
                return ResponseEntity.badRequest().body(response);
            }

            category.setCategoryId(id);
            Category updatedCategory = categoryService.save(category);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "分类更新成功");
            response.put("data", updatedCategory);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("更新分类失败: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "更新分类失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable Integer id) {
        try {
            System.out.println("=== 删除分类 ===");
            System.out.println("分类ID: " + id);

            // 检查分类是否存在
            Category existingCategory = categoryService.findById(id);
            if (existingCategory == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 404);
                response.put("message", "分类不存在");
                return ResponseEntity.status(404).body(response);
            }

            categoryService.deleteById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "分类删除成功");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("删除分类失败: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "删除分类失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 根据父分类ID获取子分类
     */
    @GetMapping("/parent/{parentId}")
    public ResponseEntity<Map<String, Object>> getCategoriesByParentId(@PathVariable Integer parentId) {
        try {
            System.out.println("=== 获取子分类 ===");
            System.out.println("父分类ID: " + parentId);

            List<Category> categories = categoryService.findByParentId(parentId);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", categories);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("获取子分类失败: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取子分类失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 搜索分类
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchCategories(@RequestParam String keyword) {
        try {
            System.out.println("=== 搜索分类 ===");
            System.out.println("关键词: " + keyword);

            List<Category> categories = categoryService.searchByCategoryName(keyword);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", categories);
            response.put("total", categories.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("搜索分类失败: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "搜索分类���败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 获取一级分类
     */
    @GetMapping("/top-level")
    public ResponseEntity<Map<String, Object>> getTopLevelCategories() {
        try {
            System.out.println("=== 获取一级分类 ===");

            List<Category> categories = categoryService.findTopLevelCategories();

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", categories);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("获取一级分类失败: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取一级分类失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 检查分类名称是否存在
     */
    @GetMapping("/check-name")
    public ResponseEntity<Map<String, Object>> checkCategoryNameExists(@RequestParam String categoryName) {
        try {
            System.out.println("=== 检查分类名称 ===");
            System.out.println("分类名称: " + categoryName);

            boolean exists = categoryService.existsByCategoryName(categoryName);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("exists", exists);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("检查分类名称失败: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "检查分类名称失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}