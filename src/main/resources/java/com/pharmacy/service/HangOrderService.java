package com.pharmacy.service;

import com.pharmacy.entity.HangOrder;

import java.util.List;
import java.util.Map;

public interface HangOrderService {
    List<HangOrder> findAll();
    HangOrder findById(String hangId);
    List<HangOrder> findByCashierId(Integer cashierId);
    List<HangOrder> findActiveByCashierId(Integer cashierId);
    HangOrder createHangOrder(Map<String, Object> hangOrderData);
    void delete(String hangId);
    Object checkoutHangOrder(String hangId, Map<String, Object> checkoutData);
}