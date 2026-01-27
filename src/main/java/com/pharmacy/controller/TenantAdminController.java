package com.pharmacy.controller;

import com.pharmacy.multitenant.MultiTenantDataSourceConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/tenants")
public class TenantAdminController {

    private final MultiTenantDataSourceConfig multiTenantConfig;
    public TenantAdminController(MultiTenantDataSourceConfig multiTenantConfig){
        this.multiTenantConfig = multiTenantConfig;
    }

    @GetMapping
    public ResponseEntity<?> list(){
        Set<String> ids = multiTenantConfig.getTenantIds();
        return ResponseEntity.ok(Map.of("tenants", ids));
    }

    @PostMapping
    public ResponseEntity<?> add(@RequestBody Map<String,String> body){
        String id = body.get("id");
        String url = body.get("url");
        String username = body.getOrDefault("username", "root");
        String password = body.getOrDefault("password", "123456");
        if(id==null || url==null){
            return ResponseEntity.badRequest().body(Map.of("error","id and url required"));
        }
        multiTenantConfig.addTenant(id,url,username,password);
        return ResponseEntity.ok(Map.of("message","tenant added","id",id));
    }
}

