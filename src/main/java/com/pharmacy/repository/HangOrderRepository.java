package com.pharmacy.repository;

import com.pharmacy.entity.HangOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HangOrderRepository extends JpaRepository<HangOrder, String> {
    List<HangOrder> findAllByOrderByHangTimeDesc();

    List<HangOrder> findByCashierId(Integer cashierId);

    @Query("SELECT h FROM HangOrder h WHERE h.cashierId = :cashierId AND h.status = 1")
    List<HangOrder> findActiveByCashierId(Integer cashierId);
}
