package com.pharmacy.repository;

import com.pharmacy.entity.PointReward;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PointRewardRepository extends JpaRepository<PointReward, Long> {
    List<PointReward> findByIsActiveTrueOrderByPointsRequiredAsc();
}

