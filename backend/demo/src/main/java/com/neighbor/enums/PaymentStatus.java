package com.neighbor.enums;

public enum PaymentStatus {
    PENDING("待支付"),
    PAID("已支付"),
    REFUNDING("退款中"),
    REFUNDED("已退款"),
    REFUND_FAILED("退款失败"),
    CLOSED("已关闭");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
