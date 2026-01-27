package com.pharmacy.controller;

import com.pharmacy.repository.EmployeeRepository;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DatabaseCheckController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private MemberRepository memberRepository;

    @GetMapping("/database-status")
    public ResponseEntity<Map<String, Object>> checkDatabase() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 检查员工表
            long employeeCount = employeeRepository.count();
            result.put("employeeCount", employeeCount);
            result.put("employees", employeeRepository.findAll());

            // 检查药品表
            long medicineCount = medicineRepository.count();
            result.put("medicineCount", medicineCount);

            // 检查会员表
            long memberCount = memberRepository.count();
            result.put("memberCount", memberCount);

            result.put("databaseStatus", "OK");
            result.put("message", "数据库连接正常，所有表数据正常");

        } catch (Exception e) {
            result.put("databaseStatus", "ERROR");
            result.put("message", "数据库连接失败: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }
}