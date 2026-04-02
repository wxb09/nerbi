package com.neighbor.dto;

import java.time.LocalDate;

public record BorrowRequest(
    Long itemId,
    LocalDate startDate,
    LocalDate endDate,
    String purpose,
    Boolean agreeTerms
) {}
