package com.neighbor.enums;

public enum ItemStatus {
    DRAFT("草稿"),
    PENDING_REVIEW("待审核"),
    AVAILABLE("可借"),
    BORROWED("借出中"),
    OFFLINE("已下架"),
    DELETED("已删除");

    private final String description;

    ItemStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
