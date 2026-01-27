package com.pharmacy.repository;

import com.pharmacy.entity.StockIn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockInRepository extends JpaRepository<StockIn, Long> {

    Optional<StockIn> findByStockInNo(String stockInNo);

    List<StockIn> findBySupplierSupplierId(Integer supplierId);

    List<StockIn> findByStatus(Integer status);

    List<StockIn> findByStockInDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT s FROM StockIn s WHERE s.stockInNo LIKE %:keyword% OR s.supplier.supplierName LIKE %:keyword%")
    Page<StockIn> findByKeyword(String keyword, Pageable pageable);

    Page<StockIn> findAll(Pageable pageable);
}