package com.pharmacy.controller;

import com.pharmacy.entity.Category;
import com.pharmacy.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "http://localhost:8080")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCategories() {
        try {
            System.out.println("=== 获取所有分类 ===");

            List<Category> categories = categoryRepository.findAll();
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

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testConnection() {
        try {
            long count = categoryRepository.count();
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "数据库连接正常");
            response.put("categoryCount", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "数据库连接失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}