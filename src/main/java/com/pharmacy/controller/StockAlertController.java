// StockAlertController.java
package com.pharmacy.controller;

import com.pharmacy.dto.ApiResponse;
import com.pharmacy.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock-alerts")
public class StockAlertController {

    private final DashboardService dashboardService;

    @Autowired
    public StockAlertController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getStockAlerts() {
        try {
            return ResponseEntity.ok(ApiResponse.success("获取库存预警成功", dashboardService.getStockAlerts()));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("获取库存预警失败"));
        }
    }
}