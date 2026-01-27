package com.pharmacy.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface SalesService {
    List<Object[]> getHotMedicines(LocalDateTime startTime, LocalDateTime endTime);
    BigDecimal getTotalSales(LocalDateTime startTime, LocalDateTime endTime);
    List<Object[]> getMedicineSalesTrend(Long medicineId, LocalDateTime startTime, LocalDateTime endTime);
}