package com.neighbor.enums;

public enum DisputeStatus {
    PENDING("待处理"),
    INVESTIGATING("调查中"),
    RESOLVED("已解决"),
    DISMISSED("已驳回");

    private final String description;

    DisputeStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
