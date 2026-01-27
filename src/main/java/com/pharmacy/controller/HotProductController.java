// HotProductController.java
package com.pharmacy.controller;

import com.pharmacy.dto.ApiResponse;
import com.pharmacy.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hot-products")
public class HotProductController {

    private final DashboardService dashboardService;

    @Autowired
    public HotProductController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getHotProducts() {
        try {
            return ResponseEntity.ok(ApiResponse.success("获取热销药品成功", dashboardService.getTodayHotProducts()));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("获取热销药品失败"));
        }
    }
}