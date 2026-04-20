package com.neighbor.enums;

public enum TransferStatus {
    PENDING("待转账"),
    SUCCESS("转账成功"),
    FAILED("转账失败");

    private final String description;

    TransferStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
