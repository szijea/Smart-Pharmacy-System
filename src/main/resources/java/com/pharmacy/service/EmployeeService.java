package com.pharmacy.service;

import com.pharmacy.entity.Employee;
import com.pharmacy.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public List<Employee> findAll() {
        return employeeRepository.findAllActive();
    }

    public Optional<Employee> findById(Integer id) {
        return employeeRepository.findById(id);
    }

    public Employee save(Employee employee) {
        // 如果是新增且密码为空，设置默认密码
        if (employee.getEmployeeId() == null && employee.getPassword() == null) {
            employee.setPassword(md5("123456")); // 默认密码
        } else if (employee.getPassword() != null && !employee.getPassword().startsWith("$2a$")) {
            // 如果密码不是BCrypt加密格式，进行MD5加密
            employee.setPassword(md5(employee.getPassword()));
        }
        return employeeRepository.save(employee);
    }

    public void delete(Integer id) {
        employeeRepository.deleteById(id);
    }

    public boolean existsByUsername(String username) {
        return employeeRepository.existsByUsername(username);
    }

    public List<Employee> findByRoleId(Integer roleId) {
        return employeeRepository.findByRoleId(roleId);
    }

    private String md5(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            return null;
        }
    }
}