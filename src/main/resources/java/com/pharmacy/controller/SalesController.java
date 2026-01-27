package com.pharmacy.controller;

import com.pharmacy.dto.HotMedicineDTO;
import com.pharmacy.service.SalesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sales")
public class SalesController {

    @Autowired
    private SalesService salesService;

    // 修复SalesController中的getHotMedicines方法
    @GetMapping("/hot-medicines")
    public ResponseEntity<List<HotMedicineDTO>> getHotMedicines(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        List<Object[]> results = salesService.getHotMedicines(startTime, endTime);
        List<HotMedicineDTO> hotMedicines = results.stream()
                .map(item -> {
                    HotMedicineDTO dto = new HotMedicineDTO();
                    // 根据实际查询结果调整映射
                    // 假设查询返回: [药品名称, 总销量, 总销售额]
                    dto.setMedicineName((String) item[0]);
                    dto.setSalesVolume(((Number) item[1]).intValue());
                    dto.setSalesAmount(((Number) item[2]).doubleValue());
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(hotMedicines);
    }
}