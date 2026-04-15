package com.neighbor.enums;

public enum DepositDisputeStatus {
    PENDING("待处理"),
    PROCESSING("处理中"),
    APPROVED("已同意扣款"),
    REJECTED("已驳回"),
    CANCELLED("已撤销");

    private final String description;

    DepositDisputeStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
