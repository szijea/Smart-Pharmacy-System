package com.pharmacy.shared.service;

import com.pharmacy.shared.entity.Boss;
import com.pharmacy.shared.repository.BossRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BossService {

    @Autowired
    private BossRepository bossRepository;

    public Optional<Boss> findByUsername(String username) {
        String original = com.pharmacy.multitenant.TenantContext.getTenant();
        try {
            com.pharmacy.multitenant.TenantContext.setTenant("default");
            return bossRepository.findByUsername(username);
        } finally {
             com.pharmacy.multitenant.TenantContext.setTenant(original);
        }
    }

    @Transactional
    public Boss createBoss(String username, String password) {
        if (bossRepository.findByUsername(username).isEmpty()) {
            Boss boss = new Boss(username, password);
            return bossRepository.save(boss);
        }
        return null;
    }

    @Transactional
    public boolean updatePassword(String username, String newPassword) {
        Optional<Boss> bossOpt = bossRepository.findByUsername(username);
        if (bossOpt.isPresent()) {
            Boss boss = bossOpt.get();
            boss.setPassword(newPassword);
            bossRepository.save(boss);
            return true;
        }
        return false;
    }
}
