// ExportController.java - 改进版本
package com.pharmacy.controller;

import com.pharmacy.dto.ApiResponse;
import com.pharmacy.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    private final DashboardService dashboardService;

    @Autowired
    public ExportController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @PostMapping("/dashboard-report")
    public ResponseEntity<byte[]> exportDashboardReport() {
        try {
            Map<String, Object> exportData = dashboardService.getExportData();

            // 将数据转换为JSON字符串
            String jsonData = convertToJson(exportData);

            // 创建文件名
            String fileName = "药房控制台报表_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".json";

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(jsonData.getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            // 返回错误响应
            String errorJson = "{\"error\": \"报表导出失败: " + e.getMessage() + "\"}";
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorJson.getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * 简单的JSON转换方法
     * 实际项目中可以使用Jackson等JSON库
     */
    private String convertToJson(Map<String, Object> data) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");

        boolean first = true;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) {
                json.append(",\n");
            }
            json.append("  \"").append(entry.getKey()).append("\": ");

            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(escapeJsonString(value.toString())).append("\"");
            } else if (value instanceof Number || value instanceof Boolean) {
                json.append(value);
            } else {
                // 对于复杂对象，简单转换为字符串
                json.append("\"").append(escapeJsonString(value.toString())).append("\"");
            }

            first = false;
        }

        json.append("\n}");
        return json.toString();
    }

    /**
     * 转义JSON字符串中的特殊字符
     */
    private String escapeJsonString(String str) {
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}