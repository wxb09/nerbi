package com.neighbor.dto;

public record CategoryDTO(
    Long id,
    String name,
    String icon,
    Integer sortOrder
) {
}
