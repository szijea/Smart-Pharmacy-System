package com.pharmacy.service;

import com.pharmacy.entity.Employee;
import com.pharmacy.entity.Role;
import com.pharmacy.repository.EmployeeRepository;
import com.pharmacy.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RoleRepository roleRepository;

    public Map<String, Object> login(String username, String password) {
        Map<String, Object> result = new HashMap<>();

        Employee employee = employeeRepository.findByUsername(username).orElse(null);
        if (employee == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        if (employee.getStatus() != 1) {
            result.put("success", false);
            result.put("message", "账户已被禁用");
            return result;
        }

        String encryptedPassword = md5(password);
        if (!employee.getPassword().equals(encryptedPassword)) {
            result.put("success", false);
            result.put("message", "密码错误");
            return result;
        }

        result.put("success", true);
        result.put("message", "登录成功");

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", employee.getEmployeeId());
        userInfo.put("username", employee.getUsername());
        userInfo.put("name", employee.getName());
        userInfo.put("roleId", employee.getRoleId());
        Role role = null;
        try {
            if (employee.getRoleId() != null) {
                role = roleRepository.findById(employee.getRoleId()).orElse(null);
            }
        } catch (Exception ignored) {
        }
        if (role != null) {
            userInfo.put("roleName", role.getRoleName());
            userInfo.put("permissions", role.getPermissions());
        }
        userInfo.put("phone", employee.getPhone());

        result.put("user", userInfo);
        result.put("token", "pharmacy-token-" + System.currentTimeMillis());

        return result;
    }

    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}