package com.neighbor.audit.dto;

public record SensitiveWordDTO(
    Long id,
    String word,
    String category,
    Integer severity,
    Integer status,
    String createdAt
) {}
