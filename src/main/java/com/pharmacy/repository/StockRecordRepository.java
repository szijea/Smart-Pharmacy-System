package com.pharmacy.repository;

import com.pharmacy.entity.StockRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockRecordRepository extends JpaRepository<StockRecord, Integer> {

    List<StockRecord> findByMedicineId(String medicineId);

    @Query("SELECT sr FROM StockRecord sr WHERE sr.createTime BETWEEN :startTime AND :endTime")
    List<StockRecord> findByCreateTimeBetween(@Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime);

    @Query("SELECT sr FROM StockRecord sr WHERE sr.operatorId = :operatorId")
    List<StockRecord> findByOperatorId(@Param("operatorId") Integer operatorId);
}