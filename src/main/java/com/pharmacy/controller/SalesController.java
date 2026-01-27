package com.pharmacy.controller;

import com.pharmacy.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/sales")
public class SalesController {

    @Autowired
    private OrderRepository orderRepository;

    @GetMapping("/trend")
    public ResponseEntity<Map<String,Object>> trend(@RequestParam(defaultValue = "14") int days){
        if(days <= 0) days = 14;
        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for(int i = days - 1; i >= 0; i--){
            LocalDate d = today.minusDays(i);
            LocalDateTime start = d.atStartOfDay();
            LocalDateTime end = d.plusDays(1).atStartOfDay();
            Double paid = safe(orderRepository.getPaidSalesBetween(start, end));
            Double refunded = safe(orderRepository.getRefundedAmountBetween(start, end));
            double net = Math.max(0.0, paid - refunded);
            labels.add(d.toString());
            values.add(net);
        }
        Map<String,Object> resp = new HashMap<>();
        resp.put("data", Map.of("labels", labels, "values", values));
        return ResponseEntity.ok(resp);
    }

    private Double safe(Double d){ return d == null? 0.0: d; }
}

