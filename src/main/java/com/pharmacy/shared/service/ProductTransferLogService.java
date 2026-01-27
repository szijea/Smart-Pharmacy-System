package com.pharmacy.shared.service;

import com.pharmacy.shared.entity.ProductTransferLog;
import com.pharmacy.shared.repository.ProductTransferLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductTransferLogService {

    @Autowired
    private ProductTransferLogRepository repository;

    public ProductTransferLog saveLog(ProductTransferLog log) {
        return repository.save(log);
    }

    public List<ProductTransferLog> getAllLogs() {
        return repository.findAll();
    }
}
