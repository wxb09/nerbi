package com.neighbor.enums;

public enum RatingTag {
    AS_DESCRIBED("与描述相符"),
    BETTER_THAN_DESCRIBED("比描述更好"),
    SLIGHT_WEAR("轻微磨损"),
    NOTICEABLE_WEAR("明显磨损"),
    NOT_AS_DESCRIBED("与描述不符");

    private final String description;

    RatingTag(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
