package com.pharmacy;

import com.pharmacy.entity.Order;
import com.pharmacy.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class OrderRepositoryAggregateTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void testAggregateMemberConsumption() {
        // prepare data
        Order o1 = new Order();
        o1.setOrderId("O1");
        o1.setCashierId(1);
        o1.setMemberId("M001");
        o1.setTotalAmount(100.0);
        o1.setActualPayment(100.0);
        o1.setPaymentType(1);
        o1.setPaymentStatus(1);
        o1.setOrderTime(LocalDateTime.now().minusHours(2));
        orderRepository.save(o1);

        Order o2 = new Order();
        o2.setOrderId("O2");
        o2.setCashierId(1);
        o2.setMemberId("M001");
        o2.setTotalAmount(50.0);
        o2.setActualPayment(50.0);
        o2.setPaymentType(1);
        o2.setPaymentStatus(1);
        o2.setOrderTime(LocalDateTime.now().minusHours(1));
        orderRepository.save(o2);

        Order o3 = new Order();
        o3.setOrderId("O3");
        o3.setCashierId(1);
        o3.setMemberId("M002");
        o3.setTotalAmount(30.0);
        o3.setActualPayment(30.0);
        o3.setPaymentType(1);
        o3.setPaymentStatus(1);
        o3.setOrderTime(LocalDateTime.now().minusMinutes(30));
        orderRepository.save(o3);

        List<Object[]> agg = orderRepository.aggregateMemberConsumption(List.of("M001","M002"));
        assertEquals(2, agg.size());
        for(Object[] r: agg){
            String mid = (String) r[0];
            Long cnt = (Long) r[1];
            assertTrue(mid.equals("M001") || mid.equals("M002"));
            if(mid.equals("M001")) assertEquals(2L, cnt);
            if(mid.equals("M002")) assertEquals(1L, cnt);
            assertNotNull(r[2]); // last order time
        }
    }
}
