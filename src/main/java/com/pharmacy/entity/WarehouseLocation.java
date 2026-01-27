package com.pharmacy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse_location")
public class WarehouseLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "location_code", nullable = false, unique = true, length = 20)
    private String locationCode;

    @Column(name = "location_name", nullable = false, length = 50)
    private String locationName;

    @Column(name = "area", length = 20)
    private String area; // 区域：A区、B区等

    @Column(name = "shelf", length = 20)
    private String shelf; // 货架：01架、02架等

    @Column(name = "layer")
    private Integer layer; // 层数

    @Column(name = "position")
    private Integer position; // 位置

    @Column(name = "capacity")
    private Integer capacity; // 容量

    @Column(name = "current_quantity")
    private Integer currentQuantity = 0; // 当前数量

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    // 构造方法
    public WarehouseLocation() {}

    public WarehouseLocation(String locationCode, String locationName, String area, String shelf) {
        this.locationCode = locationCode;
        this.locationName = locationName;
        this.area = area;
        this.shelf = shelf;
    }

    // Getter和Setter
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }

    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getShelf() { return shelf; }
    public void setShelf(String shelf) { this.shelf = shelf; }

    public Integer getLayer() { return layer; }
    public void setLayer(Integer layer) { this.layer = layer; }

    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public Integer getCurrentQuantity() { return currentQuantity; }
    public void setCurrentQuantity(Integer currentQuantity) { this.currentQuantity = currentQuantity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    // 检查是否还有空间
    public boolean hasSpace() {
        return capacity == null || currentQuantity < capacity;
    }

    // 获取剩余空间
    public Integer getRemainingSpace() {
        return capacity == null ? Integer.MAX_VALUE : capacity - currentQuantity;
    }

    @Override
    public String toString() {
        return locationName + " (" + area + "-" + shelf + ")";
    }
}