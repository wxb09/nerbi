package com.neighbor.enums;

public enum InitiatorType {
    LENDER("借出者"),
    BORROWER("借入者");

    private final String description;

    InitiatorType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
