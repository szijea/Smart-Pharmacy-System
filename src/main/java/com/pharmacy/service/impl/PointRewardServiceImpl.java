package com.pharmacy.service.impl;

import com.pharmacy.entity.PointReward;
import com.pharmacy.repository.PointRewardRepository;
import com.pharmacy.service.PointRewardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PointRewardServiceImpl implements PointRewardService {

    @Autowired
    private PointRewardRepository pointRewardRepository;

    @Override
    public List<PointReward> getAllRewards() {
        return pointRewardRepository.findAll();
    }

    @Override
    public List<PointReward> getActiveRewards() {
        return pointRewardRepository.findByIsActiveTrueOrderByPointsRequiredAsc();
    }

    @Override
    @Transactional
    public PointReward createReward(PointReward reward) {
        return pointRewardRepository.save(reward);
    }

    @Override
    @Transactional
    public PointReward updateReward(Long id, PointReward rewardDetails) {
        PointReward reward = pointRewardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reward not found"));

        reward.setName(rewardDetails.getName());
        reward.setPointsRequired(rewardDetails.getPointsRequired());
        reward.setDescription(rewardDetails.getDescription());
        reward.setIsActive(rewardDetails.getIsActive());

        return pointRewardRepository.save(reward);
    }

    @Override
    @Transactional
    public void deleteReward(Long id) {
        pointRewardRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void toggleStatus(Long id) {
        PointReward reward = pointRewardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reward not found"));
        reward.setIsActive(!reward.getIsActive());
        pointRewardRepository.save(reward);
    }
}

