package com.pharmacy.repository;

import com.pharmacy.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Integer> {

    Optional<Supplier> findBySupplierName(String supplierName);

    List<Supplier> findByContactPersonContaining(String contactPerson);

    List<Supplier> findByPhone(String phone);

    @Query("SELECT s FROM Supplier s WHERE s.supplierName LIKE %:keyword% OR s.contactPerson LIKE %:keyword%")
    List<Supplier> findByKeyword(String keyword);
}