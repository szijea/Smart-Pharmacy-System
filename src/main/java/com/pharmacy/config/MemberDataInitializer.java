package com.pharmacy.config;

import com.pharmacy.entity.Member;
import com.pharmacy.repository.MemberRepository;
import com.pharmacy.multitenant.TenantContext;
import com.pharmacy.multitenant.MultiTenantDataSourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class MemberDataInitializer implements CommandLineRunner {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MultiTenantDataSourceConfig multiTenantDataSourceConfig;

    @Override
    public void run(String... args) throws Exception {
        java.util.Collection<String> tenantSet = multiTenantDataSourceConfig.getTenantIds();
        // 如果无法获取租户列表（例如数据源配置未就绪），则手动列表
        List<String> tenants;
        if(tenantSet == null || tenantSet.isEmpty()){
            tenants = Arrays.asList("bht", "wx", "rzt_db", "default");
        } else {
            tenants = new java.util.ArrayList<>(tenantSet);
        }

        for (String tenant : tenants) {
            try {
                TenantContext.setTenant(tenant);
                initMembersForTenant(tenant);
            } catch (Exception e) {
                System.err.println("Init members failed for tenant: " + tenant + " " + e.getMessage());
            } finally {
                TenantContext.clear();
            }
        }
    }

    @Transactional
    public void initMembersForTenant(String tenant) {
        if (memberRepository.count() > 0) {
            System.out.println("Members already exist for tenant: " + tenant);
            return;
        }

        System.out.println("Initializing test members for tenant: " + tenant);

        createMember("M001", "张三", "13800138000", 1, 500, tenant);
        createMember("M002", "李四", "13900139000", 2, 1200, tenant);
        createMember("M003", "王五", "13700137000", 4, 5000, tenant);
        createMember("M004", "赵六", "13600136000", 0, 0, tenant);
    }

    private void createMember(String id, String name, String phone, int level, int points, String tenant) {
        Member m = new Member();
        m.setMemberId(id);
        m.setName(name);
        m.setPhone(phone);
        m.setLevel(level);
        m.setPoints(points);
        m.setCreateTime(LocalDateTime.now());
        // m.setStatus("ACTIVE");

        // 如果 Member 实体没有 status 字段，JPA 会忽略或报错，需检查 Member.java
        // 先只设置基本字段
        try {
            memberRepository.save(m);
        } catch (Exception e) {
            System.err.println("Failed to save member " + name + " in " + tenant + ": " + e.getMessage());
        }
    }
}

