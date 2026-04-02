package com.neighbor.dto;

public record ApproveRequest(
    Boolean approved,
    String reason
) {}
