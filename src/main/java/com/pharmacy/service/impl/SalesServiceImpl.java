package com.pharmacy.service.impl;

import com.pharmacy.repository.SaleRecordRepository;
import com.pharmacy.service.SalesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SalesServiceImpl implements SalesService {

    @Autowired
    private SaleRecordRepository saleRecordRepository;

    @Override
    public List<Object[]> getHotMedicines(LocalDateTime startTime, LocalDateTime endTime) {
        return saleRecordRepository.findHotMedicines(startTime, endTime);
    }

    @Override
    public BigDecimal getTotalSales(LocalDateTime startTime, LocalDateTime endTime) {
        return saleRecordRepository.getTotalSales(startTime, endTime);
    }

    @Override
    public List<Object[]> getMedicineSalesTrend(Long medicineId, LocalDateTime startTime, LocalDateTime endTime) {
        return saleRecordRepository.findSalesTrend(medicineId, startTime, endTime);
    }
}