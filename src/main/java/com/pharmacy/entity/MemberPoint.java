package com.pharmacy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "member_point")
public class MemberPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "member_id", nullable = false, length = 32)
    private String memberId;

    @Column(name = "point", nullable = false)
    private Integer point;

    @Column(name = "type", nullable = false)
    private Integer type;

    @Column(name = "related_order_id", length = 32)
    private String relatedOrderId;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    // 关联会员信息
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", insertable = false, updatable = false)
    private Member member;

    // 构造方法、Getter和Setter
    public MemberPoint() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public Integer getPoint() { return point; }
    public void setPoint(Integer point) { this.point = point; }

    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }

    public String getRelatedOrderId() { return relatedOrderId; }
    public void setRelatedOrderId(String relatedOrderId) { this.relatedOrderId = relatedOrderId; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }

    @PrePersist
    public void prePersist() {
        if (this.createTime == null) {
            this.createTime = LocalDateTime.now();
        }
    }
}