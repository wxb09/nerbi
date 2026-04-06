package com.neighbor.enums;

public enum MessageType {
    SYSTEM("系统通知"),
    BORROW_APPLY("借阅申请"),
    BORROW_APPROVED("借阅已同意"),
    BORROW_REJECTED("借阅已拒绝"),
    RETURN_DUE("归还提醒"),
    RETURN_OVERDUE("超期提醒"),
    RETURN_CONFIRM("归还确认");

    private final String description;

    MessageType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
