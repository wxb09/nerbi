package com.neighbor.chat.enums;

public enum MessageType {
    TEXT("文本消息"),
    IMAGE("图片消息");

    private final String description;

    MessageType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
