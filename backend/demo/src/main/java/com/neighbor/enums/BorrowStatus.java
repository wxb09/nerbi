package com.neighbor.enums;

public enum BorrowStatus {
    PENDING("待审批"),
    APPROVED("已同意"),
    ACTIVE("借用中"),
    RETURN_REQUESTED("申请归还"),
    RETURNED("已归还"),
    OVERDUE("已超期"),
    REJECTED("已拒绝"),
    CANCELLED("已取消");

    private final String description;

    BorrowStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
