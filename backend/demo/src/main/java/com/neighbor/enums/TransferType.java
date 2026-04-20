package com.neighbor.enums;

public enum TransferType {
    RENT("租金转账"),
    DEDUCTION("扣款转账");

    private final String description;

    TransferType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
