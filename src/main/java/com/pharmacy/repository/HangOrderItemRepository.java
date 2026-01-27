package com.pharmacy.repository;

import com.pharmacy.entity.HangOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HangOrderItemRepository extends JpaRepository<HangOrderItem, Integer> {

    List<HangOrderItem> findByHangId(String hangId);

    void deleteByHangId(String hangId);
}