package com.pharmacy.service;

import com.pharmacy.entity.PointReward;
import java.util.List;

public interface PointRewardService {
    List<PointReward> getAllRewards();
    List<PointReward> getActiveRewards();
    PointReward createReward(PointReward reward);
    PointReward updateReward(Long id, PointReward reward);
    void deleteReward(Long id);
    void toggleStatus(Long id);
}

