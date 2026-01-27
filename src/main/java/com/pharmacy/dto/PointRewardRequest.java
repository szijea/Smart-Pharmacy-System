package com.pharmacy.dto;

public class PointRewardRequest {
    private String name;
    private Integer pointsRequired;
    private String description;
    private Boolean isActive;

    public PointRewardRequest() {
    }

    public PointRewardRequest(String name, Integer pointsRequired, String description, Boolean isActive) {
        this.name = name;
        this.pointsRequired = pointsRequired;
        this.description = description;
        this.isActive = isActive;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPointsRequired() {
        return pointsRequired;
    }

    public void setPointsRequired(Integer pointsRequired) {
        this.pointsRequired = pointsRequired;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("isActive")
    public Boolean getIsActive() {
        return isActive;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("isActive")
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}

