package com.neighbor.enums;

public enum PaymentType {
    RENT("租金"),
    DEPOSIT("押金"),
    RENT_AND_DEPOSIT("租金+押金");

    private final String description;

    PaymentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
