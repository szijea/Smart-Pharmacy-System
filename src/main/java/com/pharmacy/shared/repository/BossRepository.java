package com.pharmacy.shared.repository;

import com.pharmacy.shared.entity.Boss;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BossRepository extends JpaRepository<Boss, Long> {
    Optional<Boss> findByUsername(String username);
}
