package com.neighbor.enums;

public enum UserStatus {
    ACTIVE("正常"),
    BANNED("已封禁"),
    DELETED("已删除");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
