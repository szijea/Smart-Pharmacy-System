package com.pharmacy.repository;

import com.pharmacy.entity.SaleRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleRecordRepository extends JpaRepository<SaleRecord, Long> {

    @Query("SELECT sr.medicineId, SUM(sr.quantity) as totalQuantity " +
            "FROM SaleRecord sr " +
            "WHERE sr.saleTime BETWEEN :startTime AND :endTime " +
            "GROUP BY sr.medicineId " +
            "ORDER BY totalQuantity DESC")
    List<Object[]> findHotMedicines(@Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COALESCE(SUM(sr.totalPrice), 0) FROM SaleRecord sr " +
            "WHERE sr.saleTime BETWEEN :startTime AND :endTime")
    BigDecimal getTotalSales(@Param("startTime") LocalDateTime startTime,
                             @Param("endTime") LocalDateTime endTime);

    @Query("SELECT DATE(sr.saleTime), SUM(sr.quantity) " +
            "FROM SaleRecord sr " +
            "WHERE sr.medicineId = :medicineId AND sr.saleTime BETWEEN :startTime AND :endTime " +
            "GROUP BY DATE(sr.saleTime) " +
            "ORDER BY DATE(sr.saleTime)")
    List<Object[]> findSalesTrend(@Param("medicineId") Long medicineId,
                                  @Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime);
}