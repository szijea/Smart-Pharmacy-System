// HangOrderServiceImpl.java - 修复类型安全警告
package com.pharmacy.service.impl;

import com.pharmacy.entity.HangOrder;
import com.pharmacy.entity.HangOrderItem;
import com.pharmacy.repository.HangOrderRepository;
import com.pharmacy.repository.HangOrderItemRepository;
import com.pharmacy.service.HangOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class HangOrderServiceImpl implements HangOrderService {

    @Autowired
    private HangOrderRepository hangOrderRepository;

    @Autowired
    private HangOrderItemRepository hangOrderItemRepository;

    @Override
    public List<HangOrder> findAll() {
        return hangOrderRepository.findAll();
    }

    @Override
    public HangOrder findById(String hangId) {
        return hangOrderRepository.findById(hangId).orElse(null);
    }

    @Override
    public List<HangOrder> findByCashierId(Integer cashierId) {
        return hangOrderRepository.findByCashierId(cashierId);
    }

    @Override
    public List<HangOrder> findActiveByCashierId(Integer cashierId) {
        return hangOrderRepository.findActiveByCashierId(cashierId);
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")  // 添加这个注解
    public HangOrder createHangOrder(Map<String, Object> hangOrderData) {
        String hangId = "H" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8);

        HangOrder hangOrder = new HangOrder();
        hangOrder.setHangId(hangId);
        hangOrder.setCashierId((Integer) hangOrderData.get("cashierId"));
        hangOrder.setRemark((String) hangOrderData.get("remark"));
        hangOrder.setStatus(0); // 未结算

        HangOrder savedHangOrder = hangOrderRepository.save(hangOrder);

        // 保存挂单项
        List<Map<String, Object>> items = (List<Map<String, Object>>) hangOrderData.get("items");
        if (items != null) {
            for (Map<String, Object> item : items) {
                HangOrderItem hangOrderItem = new HangOrderItem();
                hangOrderItem.setHangId(hangId);
                hangOrderItem.setMedicineId((String) item.get("medicineId"));
                hangOrderItem.setQuantity(((Number) item.get("quantity")).intValue());
                hangOrderItem.setUnitPrice(new java.math.BigDecimal(item.get("unitPrice").toString()));

                hangOrderItemRepository.save(hangOrderItem);
            }
        }

        return savedHangOrder;
    }

    @Override
    @Transactional
    public void delete(String hangId) {
        // 先删除关联的挂单项
        hangOrderItemRepository.deleteByHangId(hangId);
        // 再删除挂单
        hangOrderRepository.deleteById(hangId);
    }

    @Override
    @Transactional
    public Object checkoutHangOrder(String hangId, Map<String, Object> checkoutData) {
        HangOrder hangOrder = hangOrderRepository.findById(hangId)
                .orElseThrow(() -> new RuntimeException("挂单不存在"));

        // 获取挂单项
        List<HangOrderItem> hangOrderItems = hangOrderItemRepository.findByHangId(hangId);

        // 简化处理，直接返回成功信息
        hangOrder.setStatus(1); // 标记为已结算
        hangOrderRepository.save(hangOrder);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "挂单结算成功");
        result.put("hangOrder", hangOrder);
        result.put("items", hangOrderItems);

        return result;
    }
}