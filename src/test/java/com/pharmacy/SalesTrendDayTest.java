package com.pharmacy;

import com.pharmacy.entity.Order;
import com.pharmacy.repository.OrderRepository;
import com.pharmacy.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SalesTrendDayTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DashboardService dashboardService;

    private Order make(String id, int hour, double amount){
        Order o = new Order();
        o.setOrderId(id);
        o.setCashierId(1);
        o.setPaymentType(1);
        o.setPaymentStatus(1); // 已支付
        o.setTotalAmount(amount);
        o.setActualPayment(amount);
        o.setOrderTime(LocalDateTime.now().withHour(hour).withMinute(5).withSecond(0));
        return o;
    }

    @Test
    void testDayHourlyBuckets() {
        // 清理旧数据（H2内存，启动已create-drop，可忽略）
        // 插入不同时段订单：01,04,10,22 以及两个在04点
        orderRepository.save(make("D1",1,100));       // bucket0
        orderRepository.save(make("D2",4,50));        // bucket1
        orderRepository.save(make("D3",4,30));        // bucket1 (累计80)
        orderRepository.save(make("D4",10,200));      // bucket3
        orderRepository.save(make("D5",22,40));       // bucket7

        Map<String,Object> trend = dashboardService.getSalesTrend("day");
        assertNotNull(trend);
        Integer[] data = (Integer[]) trend.get("data");
        assertEquals(8, data.length, "应有8个3小时桶");
        assertEquals(100, data[0]);     // 0-2
        assertEquals(80, data[1]);      // 3-5
        assertEquals(0, data[2]);       // 6-8
        assertEquals(200, data[3]);     // 9-11
        assertEquals(0, data[4]);       // 12-14
        assertEquals(0, data[5]);       // 15-17
        assertEquals(0, data[6]);       // 18-20
        assertEquals(40, data[7]);      // 21-23
    }
}

