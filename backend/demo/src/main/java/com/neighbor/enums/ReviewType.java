package com.neighbor.enums;

public enum ReviewType {
    ITEM("物品评价"),
    USER("用户评价");

    private final String description;

    ReviewType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
