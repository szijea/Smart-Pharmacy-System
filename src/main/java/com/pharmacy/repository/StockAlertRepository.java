package com.pharmacy.repository;

import com.pharmacy.entity.StockAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockAlertRepository extends JpaRepository<StockAlert, Long> {

    // 根据处理状态查询
    List<StockAlert> findByIsHandled(Boolean isHandled);

    // 根据预警类型查询
    List<StockAlert> findByAlertType(Integer alertType);

    // 查询未处理的预警
    List<StockAlert> findByIsHandledFalse();

    // 查询近效期药品（30天内过期）
    @Query("SELECT sa FROM StockAlert sa WHERE sa.alertType = 2 AND sa.isHandled = false AND sa.expiryDate BETWEEN :start AND :end")
    List<StockAlert> findExpiringAlerts(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 统计未处理预警数量
    Long countByIsHandledFalse();

    // 根据药品ID查询预警
    List<StockAlert> findByMedicineId(String medicineId);

    // 根据预警类型查找未处理的预警
    List<StockAlert> findByAlertTypeAndIsHandledFalse(Integer alertType);

    // 在 StockAlertRepository.java 中添加这个方法
    List<StockAlert> findByMedicineIdAndAlertTypeAndIsHandledFalse(String medicineId, Integer alertType);


}