package com.pharmacy.service;

import com.pharmacy.entity.StockIn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StockInService {

    // 基本CRUD操作
    Page<StockIn> findAll(Pageable pageable);
    Optional<StockIn> findById(Long id);
    StockIn save(StockIn stockIn);
    void deleteById(Long id);

    // 根据入库单号查找
    Optional<StockIn> findByStockInNo(String stockInNo);

    // 根据供应商查找
    List<StockIn> findBySupplierId(Integer supplierId);

    // 根据状态查找
    List<StockIn> findByStatus(Integer status);

    // 根据日期范围查找
    List<StockIn> findByStockInDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // 搜索入库单
    Page<StockIn> searchByKeyword(String keyword, Pageable pageable);

    // 创建入库单
    StockIn createStockIn(StockIn stockIn);

    // 更新入库单
    StockIn updateStockIn(Long id, StockIn stockIn);

    // 审核入库单
    StockIn approveStockIn(Long id);

    // 取消入库单
    StockIn cancelStockIn(Long id);

    // 计算入库单总金额
    void calculateTotalAmount(StockIn stockIn);

    // 生成入库单号
    String generateStockInNo();
}