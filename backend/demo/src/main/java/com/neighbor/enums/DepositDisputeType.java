package com.neighbor.enums;

public enum DepositDisputeType {
    DAMAGE("物品损坏"),
    LOSS("物品丢失"),
    OVERDUE("逾期未还"),
    MISSING_PARTS("配件缺失"),
    OTHER("其他争议");

    private final String description;

    DepositDisputeType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
