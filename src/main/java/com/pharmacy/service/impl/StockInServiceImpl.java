package com.pharmacy.service.impl;

import com.pharmacy.entity.StockIn;
import com.pharmacy.entity.StockInItem;
import com.pharmacy.repository.StockInRepository;
import com.pharmacy.repository.StockInItemRepository;
import com.pharmacy.repository.SupplierRepository;
import com.pharmacy.service.StockInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StockInServiceImpl implements StockInService {

    @Autowired
    private StockInRepository stockInRepository;

    @Autowired
    private StockInItemRepository stockInItemRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Override
    public Page<StockIn> findAll(Pageable pageable) {
        return stockInRepository.findAll(pageable);
    }

    @Override
    public Optional<StockIn> findById(Long id) {
        return stockInRepository.findById(id);
    }

    @Override
    public StockIn save(StockIn stockIn) {
        return stockInRepository.save(stockIn);
    }

    @Override
    public void deleteById(Long id) {
        stockInRepository.deleteById(id);
    }

    @Override
    public Optional<StockIn> findByStockInNo(String stockInNo) {
        return stockInRepository.findByStockInNo(stockInNo);
    }

    @Override
    public List<StockIn> findBySupplierId(Integer supplierId) {
        return stockInRepository.findBySupplierSupplierId(supplierId);
    }

    @Override
    public List<StockIn> findByStatus(Integer status) {
        return stockInRepository.findByStatus(status);
    }

    @Override
    public List<StockIn> findByStockInDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return stockInRepository.findByStockInDateBetween(startDate, endDate);
    }

    @Override
    public Page<StockIn> searchByKeyword(String keyword, Pageable pageable) {
        return stockInRepository.findByKeyword(keyword, pageable);
    }

    @Override
    @Transactional
    public StockIn createStockIn(StockIn stockIn) {
        // 验证供应商是否存在
        if (stockIn.getSupplier() != null && stockIn.getSupplier().getSupplierId() != null) {
            if (!supplierRepository.existsById(stockIn.getSupplier().getSupplierId())) {
                throw new RuntimeException("供应商不存在");
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

        // 设置关联关系并计算总金额
        if (stockIn.getItems() != null) {
            for (StockInItem item : stockIn.getItems()) {
                item.setStockIn(stockIn);
            }
            calculateTotalAmount(stockIn);
        }

        return stockInRepository.save(stockIn);
    }

    @Override
    @Transactional
    public StockIn updateStockIn(Long id, StockIn stockInDetails) {
        Optional<StockIn> optionalStockIn = stockInRepository.findById(id);
        if (optionalStockIn.isEmpty()) {
            throw new RuntimeException("入库单不存在");
        }

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
            stockInItemRepository.deleteByStockInStockInId(id);
            stockIn.getItems().clear();

            // 添加新的明细
            for (StockInItem item : stockInDetails.getItems()) {
                item.setStockIn(stockIn);
                stockIn.getItems().add(item);
            }
        }

        // 重新计算总金额
        calculateTotalAmount(stockIn);

        return stockInRepository.save(stockIn);
    }

    @Override
    public StockIn approveStockIn(Long id) {
        Optional<StockIn> optionalStockIn = stockInRepository.findById(id);
        if (optionalStockIn.isEmpty()) {
            throw new RuntimeException("入库单不存在");
        }

        StockIn stockIn = optionalStockIn.get();
        stockIn.setStatus(1); // 已入库
        return stockInRepository.save(stockIn);
    }

    @Override
    public StockIn cancelStockIn(Long id) {
        Optional<StockIn> optionalStockIn = stockInRepository.findById(id);
        if (optionalStockIn.isEmpty()) {
            throw new RuntimeException("入库单不存在");
        }

        StockIn stockIn = optionalStockIn.get();
        stockIn.setStatus(2); // 已取消
        return stockInRepository.save(stockIn);
    }

    @Override
    public void calculateTotalAmount(StockIn stockIn) {
        if (stockIn.getItems() != null) {
            double totalAmount = stockIn.getItems().stream()
                    .mapToDouble(item -> {
                        if (item.getQuantity() != null && item.getUnitPrice() != null) {
                            return item.getQuantity() * item.getUnitPrice();
                        }
                        return 0.0;
                    })
                    .sum();
            stockIn.setTotalAmount(totalAmount);
        }
    }

    @Override
    public String generateStockInNo() {
        return "SI" + System.currentTimeMillis();
    }
}