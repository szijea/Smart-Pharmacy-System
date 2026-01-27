package com.pharmacy.controller;

import com.pharmacy.entity.StockIn;
import com.pharmacy.entity.StockInItem;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.repository.StockInItemRepository;
import com.pharmacy.repository.StockInRepository;
import com.pharmacy.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/stock-ins")
public class StockInController {

    @Autowired
    private StockInRepository stockInRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private StockInItemRepository stockInItemRepository;

    @GetMapping
    public ResponseEntity<Page<StockIn>> getStockIns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<StockIn> stockIns = stockInRepository.findAll(pageable);
            return ResponseEntity.ok(stockIns);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockIn> getStockInById(@PathVariable Long id) {
        Optional<StockIn> stockIn = stockInRepository.findById(id);
        return stockIn.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createStockIn(@RequestBody StockIn stockIn) {
        try {
            // 验证供应商是否存在
            if (stockIn.getSupplier() != null && stockIn.getSupplier().getSupplierId() != null) {
                if (!supplierRepository.existsById(stockIn.getSupplier().getSupplierId())) {
                    return ResponseEntity.badRequest().body("供应商不存在");
                }
            }

            // 验证药品是否存在并设置关联
            if (stockIn.getItems() != null) {
                for (StockInItem item : stockIn.getItems()) {
                    if (item.getMedicine() != null && item.getMedicine().getMedicineId() != null) {
                        if (!medicineRepository.existsById(item.getMedicine().getMedicineId())) {
                            return ResponseEntity.badRequest().body("药品不存在: " + item.getMedicine().getMedicineId());
                        }
                    }
                    // 设置关联关系
                    item.setStockIn(stockIn);
                }
            }

            // 设置入库时间（如果未设置）
            if (stockIn.getStockInDate() == null) {
                stockIn.setStockInDate(LocalDateTime.now());
            }

            // 生成入库单号（如果未设置）
            if (stockIn.getStockInNo() == null) {
                stockIn.setStockInNo(generateStockInNo());
            }

            // 计算总金额
            stockIn.calculateTotalAmount();

            StockIn savedStockIn = stockInRepository.save(stockIn);
            return ResponseEntity.ok(savedStockIn);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("创建入库单失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStockIn(@PathVariable Long id, @RequestBody StockIn stockInDetails) {
        try {
            Optional<StockIn> optionalStockIn = stockInRepository.findById(id);
            if (optionalStockIn.isPresent()) {
                StockIn stockIn = optionalStockIn.get();

                // 更新基本信息
                if (stockInDetails.getSupplier() != null) {
                    stockIn.setSupplier(stockInDetails.getSupplier());
                }
                if (stockInDetails.getStockInDate() != null) {
                    stockIn.setStockInDate(stockInDetails.getStockInDate());
                }
                if (stockInDetails.getRemark() != null) {
                    stockIn.setRemark(stockInDetails.getRemark());
                }
                if (stockInDetails.getStatus() != null) {
                    stockIn.setStatus(stockInDetails.getStatus());
                }

                // 更新明细项
                if (stockInDetails.getItems() != null) {
                    // 先清除原有明细
                    stockIn.getItems().clear();

                    // 添加新的明细
                    for (StockInItem item : stockInDetails.getItems()) {
                        item.setStockIn(stockIn);
                        stockIn.getItems().add(item);
                    }
                }

                // 重新计算总金额
                stockIn.calculateTotalAmount();

                StockIn updatedStockIn = stockInRepository.save(stockIn);
                return ResponseEntity.ok(updatedStockIn);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("更新入库单失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStockIn(@PathVariable Long id) {
        if (stockInRepository.existsById(id)) {
            stockInRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveStockIn(@PathVariable Long id) {
        Optional<StockIn> optionalStockIn = stockInRepository.findById(id);
        if (optionalStockIn.isPresent()) {
            StockIn stockIn = optionalStockIn.get();
            stockIn.setStatus(1); // 已入库
            stockInRepository.save(stockIn);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Page<StockIn>> searchStockIns(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<StockIn> stockIns = stockInRepository.findByKeyword(keyword, pageable);
            return ResponseEntity.ok(stockIns);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 生成入库单号
    private String generateStockInNo() {
        return "SI" + System.currentTimeMillis();
    }
}