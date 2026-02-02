package com.pharmacy.controller;

import com.pharmacy.entity.PointReward;
import com.pharmacy.service.PointRewardService;
import com.pharmacy.dto.PointRewardRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/point-rewards")
public class PointRewardController {

    @Autowired
    private PointRewardService pointRewardService;

    @GetMapping
    public ResponseEntity<List<PointReward>> getAllRewards() {
        return ResponseEntity.ok(pointRewardService.getAllRewards());
    }

    @GetMapping("/active")
    public ResponseEntity<List<PointReward>> getActiveRewards() {
        return ResponseEntity.ok(pointRewardService.getActiveRewards());
    }

    @PostMapping
    public ResponseEntity<PointReward> createReward(@RequestBody PointRewardRequest request) {
        PointReward reward = new PointReward(request.getName(), request.getPointsRequired(), request.getDescription());
        reward.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        return ResponseEntity.ok(pointRewardService.createReward(reward));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PointReward> updateReward(@PathVariable Long id, @RequestBody PointRewardRequest request) {
        PointReward reward = new PointReward(request.getName(), request.getPointsRequired(), request.getDescription());
        reward.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        return ResponseEntity.ok(pointRewardService.updateReward(id, reward));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReward(@PathVariable Long id) {
        pointRewardService.deleteReward(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/toggle")
    public ResponseEntity<Void> toggleStatus(@PathVariable Long id) {
        pointRewardService.toggleStatus(id);
        return ResponseEntity.ok().build();
    }
}
