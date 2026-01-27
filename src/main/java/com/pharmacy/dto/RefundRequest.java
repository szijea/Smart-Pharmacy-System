package com.pharmacy.dto;

public class RefundRequest {
    private String reason;

    // 构造方法
    public RefundRequest() {}

    public RefundRequest(String reason) {
        this.reason = reason;
    }

    // Getter和Setter
    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}