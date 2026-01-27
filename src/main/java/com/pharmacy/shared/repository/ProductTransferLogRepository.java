package com.pharmacy.shared.repository;

import com.pharmacy.shared.entity.ProductTransferLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductTransferLogRepository extends JpaRepository<ProductTransferLog, Long> {
}
