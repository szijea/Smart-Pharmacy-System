package com.pharmacy.repository;

import com.pharmacy.entity.HangOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HangOrderRepository extends JpaRepository<HangOrder, String> {

    List<HangOrder> findByCashierId(Integer cashierId);

    List<HangOrder> findByStatus(Integer status);

    @Query("SELECT ho FROM HangOrder ho WHERE ho.cashierId = :cashierId AND ho.status = 0")
    List<HangOrder> findActiveByCashierId(@Param("cashierId") Integer cashierId);
}