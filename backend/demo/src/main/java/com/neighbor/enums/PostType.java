package com.neighbor.enums;

public enum PostType {
    NORMAL("普通帖子"),
    THANKS("感谢信"),
    HELP("求助"),
    EXCHANGE("交换");

    private final String description;

    PostType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
